package impl;

/**
 * Обычный велосипед: энергию не потребляет.
 */
public final class Bike extends Vehicle {

    /**
     * Создаёт велосипед.
     *
     * @param number инвентарный номер
     * @param title название
     * @param ease простота для новичка, от 1 до 10
     * @param condition техническое состояние, от 0 до 100
     */
    public Bike(final long number, final String title, final int ease,
                final int condition) {
        super(number, title, ease, condition);
    }
}
