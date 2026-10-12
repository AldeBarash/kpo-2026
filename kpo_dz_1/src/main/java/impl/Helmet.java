package impl;

/**
 * Шлем: стоит на балансе, но энергию не потребляет.
 */
public final class Helmet extends InventoryItem {

    /**
     * Создаёт шлем.
     *
     * @param number инвентарный номер
     * @param title название
     */
    public Helmet(final long number, final String title) {
        super(number, title);
    }
}
