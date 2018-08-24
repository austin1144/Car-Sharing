package busdelivery;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.*;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Station extends Depot {

    private List<Parcel> passengers;
    private List<Optional<RoadUser>> buss = new ArrayList<>();
    private List<Double> heuristics = new ArrayList<>();
    private List<Double> distances = new ArrayList<>();
    private List<Double> times = new ArrayList<>();
    private RoadModel rm;
    private Optional<RoadUser> car0 ;
    private Simulator sm;
    private Double BUS_SPEED;

    public Station(Point position, int capacity, Simulator simulator,Double SPEED){
        super(position);
        setCapacity(capacity);
        sm = simulator;
        BUS_SPEED = SPEED;
    }

    @Override
    public void initRoadPDP(RoadModel roadModel,PDPModel pPdpModel) {
        this.rm = roadModel;
        car0 = Optional.absent();
        passengers = new ArrayList<>(rm.getObjectsOfType(Parcel.class));
        for(int i=0;i<passengers.size();i++){
            buss.add(car0);
            heuristics.add(0.0);
            distances.add(0.0);
            times.add(0.0);
        }

//        System.out.println("init");
//        System.out.println(passengers);
//        System.out.println(buss);
//        System.out.println(heuristics);
//        System.out.println(distances);
    }

    public void addPass(Parcel pass){
        passengers.add(pass);
        buss.add(car0);
        heuristics.add(0.0);
        distances.add(0.0);
        times.add(0.0);
//        System.out.println("add once");
//        System.out.println(passengers);
//        System.out.println(buss);
//        System.out.println(heuristics);
//        System.out.println(distances);
    }

    public void cleanpass(Parcel pass){
        int index = passengers.indexOf(pass);
        passengers.remove(index);
        buss.remove(index);
        heuristics.remove(index);
        distances.remove(index);
        times.remove(index);
//        System.out.println("delete once");
//        System.out.println(passengers);
//        System.out.println(buss);
//        System.out.println(heuristics);
//        System.out.println(distances);
    }

    public Parcel getNearestPassangerInfo(Point buspos,RoadModel rm){

        Parcel curr = RoadModels.findClosestObject(buspos,rm,Parcel.class);
        return curr;
    }

    public List<Parcel> getNearestNPass(Point buspos,RoadModel rm){
        //System.out.println("Got list of candidate(number): ");
        List<Parcel> currlist = RoadModels.findClosestObjects(buspos,rm,Parcel.class,5);
        //System.out.println(currlist);
        return currlist;
    }

    public Collection<Parcel> getPassinRadius(Point buspos, RoadModel rm){
        double radius = 10000;
        //System.out.println("Got list of candidate(radius): ");
        Collection<Parcel> currcollection = RoadModels.findObjectsWithinRadius(buspos,rm,radius,Parcel.class);
        //System.out.println(currcollection);
        List list;
        if (currcollection instanceof List)
        {
            list = (List)currcollection;
        }
        else
        {
            list = new ArrayList(currcollection);
        }
        if(buss.contains(Optional.absent())){
            Parcel noonewant =passengers.get(buss.indexOf(Optional.absent()));
            if(!list.contains(noonewant)){
                //System.out.println("Here is the first no one want: "+noonewant );
                list.add(list.size(),noonewant);
            }
        }
        //System.out.println(list);
        return currcollection;
    }

    public boolean intention_pick(RoadUser bus,Parcel pass, Double gain,Double distance ){
        int index = passengers.indexOf(pass);
        double timestamp = (double)sm.getCurrentTime();
        //System.out.println("check intention");
        //System.out.println("This is:    "+bus);
        //System.out.println("Want to pick:    "+pass+"  Num:  "+index);
        //System.out.println("with H: "+gain +"  cost: "+ distance);
        //System.out.println(passengers.get(index));
        //System.out.println(buss.get(index));
        //System.out.println(heuristics.get(index));
        //System.out.println(distances.get(index));
        //System.out.println("print time:");
        //System.out.println(timestamp);


        if(buss.get(index).isPresent()&&buss.get(index).get()==Optional.fromNullable(bus).get()){
            distances.set(index,distance);
            times.set(index,timestamp);
            //System.out.println("check: T same");
            return true ;
        }else if(!buss.get(index).isPresent()||(heuristics.get(index)-
                (distances.get(index)-(timestamp-times.get(index))*BUS_SPEED/3600000))<gain-distance){
            buss.set(index,Optional.fromNullable(bus));
            heuristics.set(index,gain);
            distances.set(index,distance);
            times.set(index,timestamp);
            //System.out.println("check: T replace");
            return true;
        }else {
            //System.out.println("check: F");
        return false;}
    }
}
