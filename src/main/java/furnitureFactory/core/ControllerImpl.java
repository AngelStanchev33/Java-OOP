package furnitureFactory.core;

import furnitureFactory.entities.factories.AdvancedFactory;
import furnitureFactory.entities.factories.Factory;
import furnitureFactory.entities.factories.OrdinaryFactory;
import furnitureFactory.entities.wood.OakWood;
import furnitureFactory.entities.wood.Wood;
import furnitureFactory.entities.workshops.DeckingWorkshop;
import furnitureFactory.entities.workshops.TableWorkshop;
import furnitureFactory.entities.workshops.Workshop;
import furnitureFactory.repositories.WoodRepository;
import furnitureFactory.repositories.WoodRepositoryImpl;
import furnitureFactory.repositories.WorkshopRepository;
import furnitureFactory.repositories.WorkshopRepositoryImpl;

import java.util.ArrayList;
import java.util.Collection;

import static furnitureFactory.common.ConstantMessages.*;
import static furnitureFactory.common.ExceptionMessages.*;

public class ControllerImpl implements Controller {
    private WoodRepository woodRepository;
    private WorkshopRepository workshopRepository;
    private Collection<Factory> factories;

    public ControllerImpl() {
        this.woodRepository = new WoodRepositoryImpl();
        this.workshopRepository = new WorkshopRepositoryImpl();
        this.factories = new ArrayList<>();
    }

    @Override
    public String buildFactory(String factoryType, String factoryName) {
        Factory factory = switch (factoryType) {
            case "OrdinaryFactory" -> new OrdinaryFactory(factoryName);
            case "AdvancedFactory" -> new AdvancedFactory(factoryName);
            default -> throw new IllegalArgumentException(INVALID_FACTORY_TYPE);
        };

        factories.stream()
                .filter(f -> f.getName().equals(factoryName))
                .findFirst()
                .ifPresent(f -> {
                    throw new NullPointerException(FACTORY_EXISTS); // Хвърляме IllegalArgumentException
                });

        factories.add(factory);

        return String.format(SUCCESSFULLY_BUILD_FACTORY_TYPE, factoryType, factoryName);
    }

    @Override
    public Factory getFactoryByName(String factoryName) {
        return factories.stream()
                .filter(factory -> factory.getName().equals(factoryName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String buildWorkshop(String workshopType, int woodCapacity) {
        Workshop workshop = switch (workshopType) {
            case "TableWorkshop" -> new TableWorkshop(woodCapacity);
            case "DeckingWorkshop" -> new DeckingWorkshop(woodCapacity);
            default -> throw new IllegalArgumentException(INVALID_WORKSHOP_TYPE);
        };

        workshopRepository.add(workshop);

        return String.format(SUCCESSFULLY_BUILD_WORKSHOP_TYPE, workshopType);
    }

    @Override
    public String addWorkshopToFactory(String factoryName, String workshopType) {
        if (workshopRepository.findByType(workshopType) == null) {
            throw new NullPointerException(String.format(NO_WORKSHOP_FOUND, workshopType));
        }

        Factory factory = this.getFactoryByName(factoryName);
        factory.getWorkshops().stream()
                .filter(w -> w.getClass().getSimpleName().equals(workshopType))
                .findFirst()
                .ifPresent(w -> {
                    throw new IllegalArgumentException(WORKSHOP_EXISTS);
                });

        if (workshopType.equals("TableWorkshop") && factory.getClass().getSimpleName().equals("OrdinaryFactory")) {
            factory.addWorkshop(workshopRepository.findByType(workshopType));
        } else if (workshopType.equals("DeckingWorkshop") && factory.getClass().getSimpleName().equals("AdvancedFactory")) {
            factory.addWorkshop(workshopRepository.findByType(workshopType));
        } else {
            return NON_SUPPORTED_WORKSHOP;
        }

        return String.format(SUCCESSFULLY_ADDED_WORKSHOP_IN_FACTORY, workshopType, factoryName);

    }

    @Override
    public String buyWoodForFactory(String woodType) {
        if (woodType.equals("OakWood")) {
            Wood wood = new OakWood();
            woodRepository.add(wood);
        } else {
            throw new IllegalArgumentException(INVALID_WOOD_TYPE);
        }

        return String.format(SUCCESSFULLY_BOUGHT_WOOD_FOR_FACTORY, woodType);
    }

    @Override
    public String addWoodToWorkshop(String factoryName, String workshopType, String woodType) {
        Factory factory = this.getFactoryByName(factoryName);
        Collection<Workshop> workshops = factory.getWorkshops();
        Workshop workshop = workshopRepository.findByType(workshopType);
        Wood wood = woodRepository.findByType(woodType);

        if (!workshops.contains(workshop)) {
            throw new NullPointerException(NO_WORKSHOP_ADDED);
        }
        if (null == wood) {
            throw new NullPointerException(String.format(NO_WOOD_FOUND, woodType));
        }

        woodRepository.remove(wood);
        workshop.addWood(wood);

        return String.format(SUCCESSFULLY_ADDED_WOOD_IN_WORKSHOP, woodType, workshopType);
    }

    @Override
    public String produceFurniture(String factoryName) {
        Factory factory = this.getFactoryByName(factoryName);
        Collection<Workshop> workshops = factory.getWorkshops();
        StringBuilder sb = new StringBuilder();

        if (workshops.isEmpty()) {
            throw new NullPointerException(String.format(THERE_ARE_NO_WORKSHOPS, factoryName));
        }

        boolean produced = false;

        for (Workshop workshop : workshops) {
            if (workshop.getWoodQuantity() < workshop.getWoodQuantityReduceFactor() && workshop.getWoodQuantity() > 0) {
                sb.append(INSUFFICIENT_WOOD);
                break;
            } else if (workshop.getWoodQuantity() <= 0) {
                workshops.remove(workshop);
                workshopRepository.remove(workshop);
                factory.getRemovedWorkshops().add(workshop);

                sb.append(String.format(WORKSHOP_STOPPED_WORKING, workshop.getClass().getSimpleName()));
                break;
            } else {
                workshop.produce();
                produced = true;
            }
        }

        if (produced) {
            sb.append(String.format(SUCCESSFUL_PRODUCTION, 1, factoryName));
        } else {
            sb.append("\n");
            sb.append(String.format(FACTORY_DO_NOT_PRODUCED, factoryName));
        }

        return sb.toString().trim();
    }

    @Override

    public String getReport() {
        StringBuilder sb = new StringBuilder();

        factories.forEach(factory -> {
            sb.append(String.format("Production by %s factory:\n", factory.getName()));
            Collection<Workshop> workshops = factory.getWorkshops();
            Collection<Workshop> removedWorkshops = factory.getRemovedWorkshops();

            if (workshops.isEmpty() && removedWorkshops.isEmpty()) {
                sb.append(" No workshops were added to produce furniture.\n");
            } else {
                if (removedWorkshops.isEmpty()){
                    for (Workshop workshop : workshops) {
                        sb.append(String.format(" %s: %d furniture produced\n",
                                workshop.getClass().getSimpleName(),workshop.getProducedFurnitureCount()));
                    }
                } else if (workshops.isEmpty()) {
                    for (Workshop removedWorkshop : removedWorkshops) {
                        sb.append(String.format(" %s: %d furniture produced\n",
                                removedWorkshop.getClass().getSimpleName(),removedWorkshop.getProducedFurnitureCount()));
                    }
                }
            }
        });

        return sb.toString().trim();
    }
}
