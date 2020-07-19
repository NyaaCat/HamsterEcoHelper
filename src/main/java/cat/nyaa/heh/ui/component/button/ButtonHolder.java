package cat.nyaa.heh.ui.component.button;

public interface ButtonHolder {
    GUIButton getButtonAt(int index);
    void setButtonAt(int index, GUIButton button);
}
