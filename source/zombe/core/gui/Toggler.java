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
        Object value = this.getValue();
        if (value == null) {
            this.setValue(true);
        } else if (value instanceof Boolean) {
            this.setValue(!(Boolean) value);
        }
    }
}
