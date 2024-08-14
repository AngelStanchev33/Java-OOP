package furnitureFactory.entities.workshops;

import furnitureFactory.entities.wood.Wood;

public abstract class BaseWorkshop implements Workshop {

    private int woodQuantity;
    private int producedFurnitureCount;
    private int woodQuantityReduceFactor;

    public BaseWorkshop(int woodQuantity, int woodQuantityReduceFactor) {
        setWoodQuantity(woodQuantity);
        this.woodQuantityReduceFactor = woodQuantityReduceFactor;
        this.producedFurnitureCount = 0;
    }

    @Override
    public int getWoodQuantity() {
        return this.woodQuantity;
    }

    @Override
    public int getProducedFurnitureCount() {
        return producedFurnitureCount;
    }

    @Override
    public int getWoodQuantityReduceFactor() {
        return woodQuantityReduceFactor;
    }

    @Override
    public void addWood(Wood wood) {
        setWoodQuantity(this.woodQuantity + wood.getWoodQuantity());
    }

    @Override
    public void produce() {
        setWoodQuantity(this.woodQuantity - this.getWoodQuantityReduceFactor());
        this.producedFurnitureCount++;
    }

    public void setWoodQuantity(int woodQuantity) {
        if (woodQuantity <= 0) {
            woodQuantity = 0;
        }
        this.woodQuantity = woodQuantity;
    }
}
