package zombe.core.ducks;

public interface IPlayerController {

    void syncCurrentItem();

    void switchToRealItem();

    void switchToIdleItem();
}
