package io.goobi.viewer.model.viewer.record.views;

import org.apache.commons.lang3.StringUtils;

public class ComparisonCondition<T extends Number> extends Condition<T> {

    protected ComparisonCondition(T value, boolean matchIfLarger) {
        super(value, matchIfLarger);
    }

    public boolean isMatchIfLarger() {
        return super.isMatchIfEqual();
    }

    public boolean matches(T testValue) {
        if (this.getValue() == null) {
            return true;
        } else if (this.isMatchIfLarger()) {
            return testValue.doubleValue() >= this.getValue().doubleValue();
        } else {
            return testValue.doubleValue() < this.getValue().doubleValue();
        }
    }

    public static ComparisonCondition<?> of(String number) {
        if (StringUtils.isBlank(number) || !number.matches("!?[\\d.]+")) {
            return new ComparisonCondition<Integer>(null, true);
        }
        boolean matchIfLarger = !number.startsWith("!");
        if (number.contains(".")) {
            Double value = Double.valueOf(number.replace("!", ""));
            return new ComparisonCondition<Double>(value, matchIfLarger);
        } else {
            Integer value = Integer.valueOf(number.replace("!", ""));
            return new ComparisonCondition<Integer>(value, matchIfLarger);
        }
    }

}
