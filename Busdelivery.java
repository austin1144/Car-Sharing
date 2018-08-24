
package busdelivery;

import static com.google.common.collect.Maps.newHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nullable;


import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.geom.io.Filters;
import org.apache.commons.math3.random.RandomGenerator;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;


public final class Busdelivery {
    // Bus and passangers
    private static final int NUM_BUS = 10;
    private static final int NUM_PASSANGER = 40;
    private static final int NUM_STATION = 1000;
    private static final int CAPACITY_BUS = 8;      /*max passenger in the bus*/
    private static final int CAPACITY_PASSANGER = 3;    /*passanger at locatino 1-3*/
    private static final double NEU_PASS = 0.007;
    private static final double BUS_SPEED = 10000d;

    // time in ms
    private static final long SERVICE_DURATION = 60000;

    private static final int SPEED_UP = 4;

    private static final String MAP_FILE = "/leuven-simple.dot";
    private static final Map<String, Graph<MultiAttributeData>> GRAPH_CACHE =
            newHashMap();

    // Time
    private static final int SPEED = 5;

    private Busdelivery(){}


    public static void main(@Nullable String[] args) {
        final long endTime = args != null && args.length >= 1 ? Long
                .parseLong(args[0]) : Long.MAX_VALUE;

        final String graphFile = args != null && args.length >= 2 ? args[1]
                : MAP_FILE;
        run( endTime, graphFile,null,null,null);
    }

    public static Simulator run(final long endTime, String graphFile,
                                @Nullable Display display,
                                @Nullable Monitor m,
                                @Nullable Listener list) {
        final View.Builder view = createGui(display,m,list);
        //generate simulator
        final Simulator simulator = Simulator.builder()
                .addModel(RoadModelBuilders.staticGraph(loadGraph(graphFile)))
                .addModel(DefaultPDPModel.builder())
                .addModel(view)
                .build();
        //generate random generator
        final RandomGenerator rng = simulator.getRandomGenerator();

        final RoadModel roadModel = simulator.getModelProvider().getModel(
                RoadModel.class);

        for (int i = 0; i < NUM_PASSANGER; i++) {
            simulator.register(new Passanger(
                    Parcel.builder(roadModel.getRandomPosition(rng),
                            roadModel.getRandomPosition(rng))
                            .serviceDuration(SERVICE_DURATION)
                            .neededCapacity(1 + rng.nextInt(CAPACITY_PASSANGER))
                            .buildDTO()));
        }
        final Station station = new Station(roadModel.getRandomPosition(rng),NUM_STATION,simulator,BUS_SPEED);
        simulator.register(station);

        for (int i = 0; i < NUM_BUS; i++) {
            simulator.register(new Bus(roadModel.getRandomPosition(rng),
                    CAPACITY_BUS,roadModel,BUS_SPEED));
        }

        simulator.addTickListener(new TickListener() {
            @Override
            public void tick(TimeLapse time) {
                if (time.getStartTime() > endTime) {
                    simulator.stop();
                } else if (rng.nextDouble() < NEU_PASS) {
                    Passanger pass = new Passanger(
                            Parcel
                                    .builder(roadModel.getRandomPosition(rng),
                                            roadModel.getRandomPosition(rng))
                                    .serviceDuration(SERVICE_DURATION)
                                    .neededCapacity(1 + rng.nextInt(CAPACITY_PASSANGER))
                                    .buildDTO());
                    simulator.register(pass);
                    station.addPass(pass);
                }
            }

            @Override
            public void afterTick(TimeLapse timeLapse) {}
        });
        simulator.start();
        return simulator;
    }

    static View.Builder createGui(@Nullable Display display,
                                  @Nullable Monitor m,
                                  @Nullable Listener list){
        View.Builder view = View.builder()
                .with(GraphRoadModelRenderer.builder())
                .with(RoadUserRenderer.builder()
                        .withImageAssociation(
                                Bus.class, "/small-bus-32.png")
                        .withImageAssociation(
                                Passanger.class,"/person-blue-32.png"))
                .with(TaxiRenderer.builder(TaxiRenderer.Language.ENGLISH))
                .withTitleAppendix("Bus Delivery");
        if (m != null && list != null && display != null) {
            view = view.withMonitor(m)
                    .withSpeedUp(SPEED)
                    .withResolution(m.getClientArea().width, m.getClientArea().height)
                    .withDisplay(display)
                    .withCallback(list)
                    .withAsync()
                    .withAutoPlay()
                    .withAutoClose();
        }
        return view;
    }

    static Graph<MultiAttributeData> loadGraph(String name){
        try{
            if(GRAPH_CACHE.containsKey(name)){
                return GRAPH_CACHE.get(name);
            }
            final Graph<MultiAttributeData> g = DotGraphIO
                    .getMultiAttributeGraphIO(
                            Filters.selfCycleFilter())
                    .read(
                            Busdelivery.class.getResourceAsStream(name));
            GRAPH_CACHE.put(name,g);
            return g;
        } catch (final FileNotFoundException e){
            throw new IllegalStateException(e);
        } catch (final IOException e){
            throw new IllegalStateException(e);
        }
    }

    static class Passanger extends Parcel{
        Passanger(ParcelDTO dto) {super(dto);}

        public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel){}
    }


}
