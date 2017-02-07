package zombe.core.gui;


public class Toggler extends Button {

    public Toggler(String name) {
        super(name);
    }

    public Toggler(String name, Object value) {
        super(name, value);
    }

    @Override
    protected void onActivation() {
        if (getValue() == null) {
            setValue((Boolean) true);
        } else if (getValue() instanceof Boolean) {
            setValue(!(Boolean) getValue());
        }
    }

}
