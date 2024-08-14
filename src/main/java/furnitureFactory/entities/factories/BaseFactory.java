package furnitureFactory.entities.factories;

import furnitureFactory.entities.workshops.Workshop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static furnitureFactory.common.ExceptionMessages.FACTORY_NAME_NULL_OR_EMPTY;

public abstract class BaseFactory implements Factory {

    private String name;
    private Collection<Workshop> workshops;
    private Collection<Workshop> removedWorkshops;

    public BaseFactory(String name) {
        setName(name);
        this.workshops = new ArrayList<>();
        this.removedWorkshops = new ArrayList<>();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void addWorkshop(Workshop workshop) {
        workshops.add(workshop);

    }

    @Override
    public Collection<Workshop> getWorkshops() {
        return this.workshops;
    }

    @Override
    public Collection<Workshop> getRemovedWorkshops() {
        return this.removedWorkshops;
    }

    public void setName(String name) {
        if (null == name || name.isEmpty() || name.isBlank()) {
            throw new NullPointerException(FACTORY_NAME_NULL_OR_EMPTY);
        }

        this.name = name;
    }
}
