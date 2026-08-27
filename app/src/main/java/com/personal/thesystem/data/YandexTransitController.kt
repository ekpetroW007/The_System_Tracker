package com.personal.thesystem.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Geo
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session as SearchSession
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.FilterVehicleTypes
import com.yandex.mapkit.transport.masstransit.FitnessOptions
import com.yandex.mapkit.transport.masstransit.MasstransitRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.RouteOptions
import com.yandex.mapkit.transport.masstransit.Session as MasstransitSession
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.mapkit.transport.masstransit.TransitOptions
import com.yandex.runtime.Error
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.ceil
import kotlin.math.roundToInt

data class TransitOption(
    val lines: String,
    val totalMinutes: Int,
    val boardingStop: String,
    val exitStop: String,
    val busArrivalTime: String,
    val walkToStopMeters: Int,
    val walkToUniversityMeters: Int,
)

data class HseTransitPlan(
    val route: TransitOption,
    val targetDate: LocalDate,
    val homeAddress: String,
    val universityAddress: String,
)

internal fun isBusLine(vehicleTypes: List<String>): Boolean = "bus" in vehicleTypes

internal fun isDirectBusRoute(busSections: Int, transfers: Int): Boolean =
    busSections == 1 && transfers == 0

internal fun walkingMinutesAtFourKmh(distanceMeters: Int): Int =
    ceil(distanceMeters.coerceAtLeast(0) / WALKING_METERS_PER_MINUTE).toInt()

internal fun adjustedTotalMinutes(
    routeSeconds: Double,
    defaultEdgeWalkingSeconds: Double,
    rideSeconds: Double,
    walkingMeters: Int,
): Int {
    val customWalkingSeconds = walkingMinutesAtFourKmh(walkingMeters) * 60.0
    val adjustedSeconds = (routeSeconds - defaultEdgeWalkingSeconds)
        .coerceAtLeast(rideSeconds) + customWalkingSeconds
    return ceil(adjustedSeconds / 60.0).toInt()
}

internal fun prioritizeNearestBusOptions(options: List<TransitOption>): List<TransitOption> {
    val closestForEachBus = options
        .groupBy { it.lines }
        .values
        .map { variants ->
            variants.minWith(
                compareBy<TransitOption> { it.walkToStopMeters }
                    .thenBy { it.walkToUniversityMeters }
                    .thenBy { it.totalMinutes },
            )
        }
    val threeNearest = closestForEachBus
        .sortedWith(compareBy<TransitOption> { it.walkToStopMeters }.thenBy { it.totalMinutes })
        .take(3)
    return threeNearest.sortedWith(
        compareBy<TransitOption> { it.totalMinutes }.thenBy { it.walkToStopMeters },
    )
}

internal fun plannedMorningDate(
    today: LocalDate,
    now: LocalTime,
    cachedDate: LocalDate?,
): LocalDate = when {
    cachedDate != null && !cachedDate.isBefore(today) -> cachedDate
    now.isBefore(HSE_ROUTE_TIME) -> today
    else -> today.plusDays(1)
}

internal fun routeDepartureEpochMillis(date: LocalDate): Long = date
    .atTime(HSE_ROUTE_TIME)
    .atZone(MOSCOW_ZONE)
    .toInstant()
    .toEpochMilli()

sealed interface TransitRoutesState {
    data object Idle : TransitRoutesState
    data object Loading : TransitRoutesState
    data class Ready(val plan: HseTransitPlan) : TransitRoutesState
    data class Failed(val message: String) : TransitRoutesState
}

class YandexTransitController {
    var state: TransitRoutesState by mutableStateOf(TransitRoutesState.Idle)
        private set

    private val searchManager: SearchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.COMBINED)
    private val router: MasstransitRouter = TransportFactory.getInstance().createMasstransitRouter()
    private var searchSession: SearchSession? = null
    private var routeSession: MasstransitSession? = null
    private var searchListener: SearchSession.SearchListener? = null
    private var routeListener: MasstransitSession.RouteListener? = null
    private var requestToken = 0

    fun showSaved(plan: HseTransitPlan) {
        searchSession?.cancel()
        routeSession?.cancel()
        state = TransitRoutesState.Ready(plan)
    }

    fun refresh(
        homeAddress: String,
        universityAddress: String,
        targetDate: LocalDate,
        onReady: (HseTransitPlan) -> Unit,
    ) {
        if (homeAddress.isBlank()) {
            state = TransitRoutesState.Idle
            return
        }
        requestToken += 1
        val token = requestToken
        searchSession?.cancel()
        routeSession?.cancel()
        state = TransitRoutesState.Loading

        geocode(homeAddress, token) { home ->
            geocode(universityAddress, token) { university ->
                requestRoutes(
                    home = home,
                    university = university,
                    homeAddress = homeAddress,
                    universityAddress = universityAddress,
                    targetDate = targetDate,
                    token = token,
                    onReady = onReady,
                )
            }
        }
    }

    private fun geocode(address: String, token: Int, onFound: (Point) -> Unit) {
        searchListener = object : SearchSession.SearchListener {
            override fun onSearchResponse(response: Response) {
                if (token != requestToken) return
                val point = response.collection.children.asSequence()
                    .mapNotNull { it.obj }
                    .flatMap { it.geometry.asSequence() }
                    .mapNotNull { it.point }
                    .firstOrNull()
                if (point == null) {
                    state = TransitRoutesState.Failed("Не удалось найти адрес на карте")
                } else {
                    onFound(point)
                }
            }

            override fun onSearchError(error: Error) {
                if (token == requestToken) {
                    state = TransitRoutesState.Failed("Не удалось найти адрес. Проверь интернет и попробуй ещё раз")
                }
            }
        }
        searchSession = searchManager.submit(
            address,
            Geometry.fromPoint(MOSCOW_CENTER),
            SearchOptions().apply {
                searchTypes = SearchType.GEO.value
                resultPageSize = 1
            },
            searchListener!!,
        )
    }

    private fun requestRoutes(
        home: Point,
        university: Point,
        homeAddress: String,
        universityAddress: String,
        targetDate: LocalDate,
        token: Int,
        onReady: (HseTransitPlan) -> Unit,
    ) {
        val points = listOf(
            RequestPoint(home, RequestPointType.WAYPOINT, null, null, null),
            RequestPoint(university, RequestPointType.WAYPOINT, null, null, null),
        )
        routeListener = object : MasstransitSession.RouteListener {
            override fun onMasstransitRoutes(routes: MutableList<Route>) {
                if (token != requestToken) return
                val route = prioritizeNearestBusOptions(
                    routes.mapNotNull { route -> toBusTransitOption(route, home, university) },
                ).firstOrNull()
                if (route == null) {
                    state = TransitRoutesState.Failed("На 08:30 прямой автобус до ВШЭ не найден")
                    return
                }
                val plan = HseTransitPlan(route, targetDate, homeAddress, universityAddress)
                onReady(plan)
                state = TransitRoutesState.Ready(plan)
            }

            override fun onMasstransitRoutesError(error: Error) {
                if (token == requestToken) {
                    state = TransitRoutesState.Failed("Маршрут не обновился. Проверь интернет и повтори")
                }
            }
        }
        routeSession = router.requestRoutes(
            points,
            TransitOptions(
                BUS_ONLY_AVOID_MASK,
                TimeOptions(routeDepartureEpochMillis(targetDate), null),
            ),
            RouteOptions(FitnessOptions(false, false)),
            routeListener!!,
        )
    }

    private fun toBusTransitOption(route: Route, home: Point, university: Point): TransitOption? {
        val indexedRideSections = route.sections.withIndex()
            .filter { it.value.metadata.data.transports != null }
        val rideSections = indexedRideSections.map { it.value }
        val busTransportsBySection = rideSections.map { section ->
            section.metadata.data.transports.orEmpty().filter { isBusLine(it.line.vehicleTypes) }
        }
        if (busTransportsBySection.isEmpty() || busTransportsBySection.any { it.isEmpty() }) return null
        if (!isDirectBusRoute(rideSections.size, route.metadata.weight.transfersCount)) return null

        val firstRide = indexedRideSections.first()
        val lastRide = indexedRideSections.last()
        val boardingStop = firstRide.value.stops.firstOrNull() ?: return null
        val exitStop = lastRide.value.stops.lastOrNull() ?: return null
        val approachSections = route.sections.take(firstRide.index)
        val exitSections = route.sections.drop(lastRide.index + 1)
        val walkToStopMeters = sectionWalkingDistance(approachSections)
            .takeIf { it > 0 }
            ?: Geo.distance(home, boardingStop.position).roundToInt()
        val walkToUniversityMeters = sectionWalkingDistance(exitSections)
            .takeIf { it > 0 }
            ?: Geo.distance(exitStop.position, university).roundToInt()

        val selectedTransport = busTransportsBySection.single()
            .firstOrNull { transport -> transport.transports.any { it.isRecommended } }
            ?: busTransportsBySection.single().first()
        val selectedThread = selectedTransport.transports.firstOrNull { it.isRecommended }
            ?: selectedTransport.transports.firstOrNull()
        val boardingStopName = selectedThread
            ?.alternateDepartureStop
            ?.name
            ?.takeIf { it.isNotBlank() }
            ?: boardingStop.metadata.stop.name.ifBlank { "Остановка рядом с домом" }
        val busArrivalTime = selectedThread
            ?.estimation
            ?.departureTime
            ?.text
            ?.takeIf { it.isNotBlank() }
            ?: firstRide.value.metadata.estimation
                ?.departureTime
                ?.text
                ?.takeIf { it.isNotBlank() }
            ?: return null
        val rideSeconds = rideSections.sumOf { it.metadata.weight.time.value }
        val originalEdgeWalkingSeconds = (approachSections + exitSections)
            .filter { it.metadata.data.fitness != null }
            .sumOf { it.metadata.weight.time.value }
        val totalMinutes = adjustedTotalMinutes(
            routeSeconds = route.metadata.weight.time.value,
            defaultEdgeWalkingSeconds = originalEdgeWalkingSeconds,
            rideSeconds = rideSeconds,
            walkingMeters = walkToStopMeters + walkToUniversityMeters,
        )
        return TransitOption(
            lines = selectedTransport.line.name.ifBlank { "без номера" },
            totalMinutes = totalMinutes,
            boardingStop = boardingStopName,
            exitStop = exitStop.metadata.stop.name.ifBlank { "Остановка рядом с ВШЭ" },
            busArrivalTime = busArrivalTime,
            walkToStopMeters = walkToStopMeters,
            walkToUniversityMeters = walkToUniversityMeters,
        )
    }

    private fun sectionWalkingDistance(sections: List<com.yandex.mapkit.transport.masstransit.Section>): Int =
        sections.sumOf { it.metadata.weight.walkingDistance.value }.roundToInt()

    companion object {
        private val MOSCOW_CENTER = Point(55.751244, 37.618423)
        private val BUS_ONLY_AVOID_MASK = FilterVehicleTypes.values()
            .filterNot { it == FilterVehicleTypes.NONE || it == FilterVehicleTypes.BUS }
            .fold(0) { mask, type -> mask or type.value }
    }
}

internal val HSE_ROUTE_TIME: LocalTime = LocalTime.of(8, 30)
private val MOSCOW_ZONE = ZoneId.of("Europe/Moscow")
private const val WALKING_METERS_PER_MINUTE = 4_000.0 / 60.0
