package service;

import exception.ApplicationErrorCode;
import exception.ApplicationException;
import interfaces.IEnergyConsumer;
import interfaces.IInventory;
import interfaces.IServiceCenter;
import interfaces.ITransport;
import interfaces.ITransportFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Учёт парка: приём техники после техосмотра, вещи на балансе,
 * отчёты по количеству и энергии, список техники для новичков.
 */
@Service
@RequiredArgsConstructor
public final class StationManager {

    /** Сервис техосмотра (внедряется контейнером, в тестах подменяется). */
    private final IServiceCenter serviceCenter;

    /** Правило «подходит новичку» (подменяется в тестах). */
    private final ITransportFilter beginnerFilter;

    /** Принятый транспорт. */
    private final List<ITransport> vehicles = new ArrayList<>();

    /** Вещи на балансе (шлемы, зарядные станции и т. п.). */
    private final List<IInventory> items = new ArrayList<>();

    /** Занятые инвентарные номера. */
    private final Set<Long> usedNumbers = new HashSet<>();

    /**
     * Принимает транспорт в парк, если он прошёл техосмотр.
     *
     * @param transport новый транспорт
     * @return {@code true}, если принят; {@code false}, если техосмотр
     *         не пройден
     * @throws ApplicationException если номер уже занят
     */
    public boolean putOnBalance(final ITransport transport) {
        if (transport == null) {
            throw new ApplicationException(
                ApplicationErrorCode.REQUIRED_VALUE_MISSING,
                "Транспорт не задан");
        }
        ensureNumberIsFree(transport);
        if (!serviceCenter.checkVehicle(transport)) {
            return false;
        }
        vehicles.add(transport);
        usedNumbers.add(transport.getInventoryNumber());
        return true;
    }

    /**
     * Ставит вещь на баланс. Транспорт сюда передавать нельзя:
     * он должен проходить техосмотр через {@link #putOnBalance(ITransport)}.
     *
     * @param item вещь (шлем, зарядная станция и т. п.)
     * @throws ApplicationException если это транспорт или номер занят
     */
    public void putOnBalance(final IInventory item) {
        if (item == null) {
            throw new ApplicationException(
                ApplicationErrorCode.REQUIRED_VALUE_MISSING,
                "Вещь не задана");
        }
        if (item instanceof ITransport) {
            throw new ApplicationException(
                ApplicationErrorCode.INVALID_ITEM_TYPE,
                "Транспорт принимается через putOnBalance(ITransport) "
                    + "(с техосмотром)");
        }
        ensureNumberIsFree(item);
        items.add(item);
        usedNumbers.add(item.getInventoryNumber());
    }

    /**
     * Возвращает принятый транспорт.
     *
     * @return неизменяемая копия списка
     */
    public List<ITransport> getVehicles() {
        return List.copyOf(vehicles);
    }

    /**
     * Возвращает вещи на балансе.
     *
     * @return неизменяемая копия списка
     */
    public List<IInventory> getItems() {
        return List.copyOf(items);
    }

    /**
     * Возвращает всё, что стоит на балансе: сначала транспорт, потом вещи.
     *
     * @return неизменяемый список
     */
    public List<IInventory> getAllInventory() {
        List<IInventory> all = new ArrayList<>(vehicles);
        all.addAll(items);
        return List.copyOf(all);
    }

    /**
     * Возвращает число единиц транспорта.
     *
     * @return количество принятого транспорта
     */
    public int getVehicleCnt() {
        return vehicles.size();
    }

    /**
     * Возвращает число вещей на балансе.
     *
     * @return количество вещей
     */
    public int getItemCnt() {
        return items.size();
    }

    /**
     * Считает единицы на балансе по видам (по имени класса).
     *
     * @return отсортированная по названию вида карта «вид - количество»
     */
    public Map<String, Long> countByType() {
        return getAllInventory().stream().collect(Collectors.groupingBy(
            item -> item.getClass().getSimpleName(),
            TreeMap::new,
            Collectors.counting()));
    }

    /**
     * Считает суммарное суточное потребление энергии всего, что на балансе.
     *
     * @return расход в кВт·ч в сутки
     */
    public double totalEnergy() {
        return getAllInventory().stream()
            .filter(IEnergyConsumer.class::isInstance)
            .map(IEnergyConsumer.class::cast)
            .mapToDouble(IEnergyConsumer::getDailyEnergyConsumption)
            .sum();
    }

    /**
     * Возвращает прокатную технику, которую можно выдавать новичкам.
     *
     * @return транспорт, подходящий под правило фильтра
     */
    public List<ITransport> transportForBeginners() {
        return vehicles.stream().filter(beginnerFilter::matches).toList();
    }

    /**
     * Проверяет, что инвентарный номер ещё не использован.
     *
     * @param item проверяемый объект
     */
    private void ensureNumberIsFree(final IInventory item) {
        if (usedNumbers.contains(item.getInventoryNumber())) {
            throw new ApplicationException(
                ApplicationErrorCode.DUPLICATE_INVENTORY_NUMBER,
                "Инвентарный номер уже занят: "
                    + item.getInventoryNumber());
        }
    }
}
