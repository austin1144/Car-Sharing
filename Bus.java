package busdelivery;

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import sun.misc.GC;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class Benifit_Path<A, B> {
    public final A cost;
    public final B path;
    public Benifit_Path(A a, B b) {
        cost = a;
        path = b;
    }
    public String toString() {
        return "(" + cost + ", " + path + ")";
    }
}

public class Bus extends Vehicle{
    private Optional<Parcel> marked;//the pass been marked--- the bus is going to pick it
    private Optional<Parcel> marked_delay;//give the marked on t delay,for checking station
    private Optional<Parcel> candidate;//the pass found on the rm
    private Optional<Parcel> lastcandidate;
    private List<Parcel> new_candidate_list = new ArrayList<>();
    private ArrayList<Parcel>  candidate_list = new ArrayList<>();//the passes found on the rm
    private ArrayList<Boolean>  candidate_flag = new ArrayList<>();//the passes checked flag
    private ArrayList<Parcel>  candidate_list_temp = new ArrayList<>();//the passes found on the rm
    private ArrayList<Boolean>  candidate_flag_temp = new ArrayList<>();//the passes checked flag
    private Set<Station> stationset;
    private Station station;
    private ArrayList<Parcel> onboard = new ArrayList<Parcel>(); //all pass s on the bus,always optimal
    private ArrayList<Parcel> onboardwithintention = new ArrayList<Parcel>();// best path to delivery all pass
    private double onboardnum = 0;

    public Bus(Point startPosition, int capacity,RoadModel rm,double SPEED){
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED)
                .build());
        marked = Optional.absent();
        marked_delay = Optional.absent();
        candidate = Optional.absent();
        lastcandidate = Optional.absent();

        stationset = rm.getObjectsOfType(Station.class);
        station = stationset.iterator().next();
    }
/* This function calcuate the path cost*/
    public double pathCost(RoadUser startpoint,ArrayList<Parcel> PC_onboard,RoadModel rm){
        double dissum =0;
        if(PC_onboard.size()==0){
            return dissum;
        }else{
            ArrayList<Point> bp = new ArrayList<Point>();
            //how to get the distance as number?
            List<Point> path_temp = rm.getShortestPathTo(startpoint, PC_onboard.get(0).getDeliveryLocation());
            Measure<Double, Length> dis;
            try {
                dis = rm.getDistanceOfPath(path_temp);
            }catch (IllegalArgumentException e){
                if(!path_temp.isEmpty()){path_temp.remove(0);}
                dis = rm.getDistanceOfPath(path_temp);
            }
            dissum = dis.getValue();
            if(PC_onboard.size()==1){
                return dissum;
            }else{
                    for (int i =0;i<PC_onboard.size()-1;i++) {
                    dis = rm.getDistanceOfPath(rm.getShortestPathTo(PC_onboard.get(i).getDeliveryLocation(),PC_onboard.get(i+1).getDeliveryLocation()));
                    dissum += dis.getValue();
                    }
                    return dissum;
                }
        }
    }
    /* This function return the best path to delivery the customer*/
    public Benifit_Path<Double,ArrayList<Parcel>> getBestPath(RoadUser startpoint, ArrayList<ArrayList<Parcel>> All_onboard, RoadModel rm) {
        ArrayList<Parcel> bp = new ArrayList<Parcel>();
        double cost = 0;
        double bestCost = Double.MAX_VALUE;
        for(int i=0;i<All_onboard.size();i++) {
            cost = pathCost(startpoint,All_onboard.get(i),rm);
            if(cost<bestCost){
                bp = All_onboard.get(i);
                bestCost = cost;
            }
        }
        return new Benifit_Path<Double,ArrayList<Parcel>>(bestCost,bp);
    }

    //Very stupid way to give out all combination of an array(Delivery order).
    private ArrayList<ArrayList<Parcel>> getCombination(ArrayList<Parcel> GC_onboard) {

        final ArrayList<ArrayList<Parcel>> All_onboard = new ArrayList<ArrayList<Parcel>>();
        if(GC_onboard.size()==1) {
            All_onboard.add(GC_onboard);
        }else if(GC_onboard.size()==2) {
            ArrayList<Parcel> GC_onboardtemp = new ArrayList<Parcel>();
            GC_onboardtemp.add(GC_onboard.get(1));
            GC_onboardtemp.add(GC_onboard.get(0));
            All_onboard.add(GC_onboard);
            All_onboard.add(GC_onboardtemp);
        }else if(GC_onboard.size()==3) {
            ArrayList<Parcel> GC_onboardtemp = new ArrayList<Parcel>();
            All_onboard.add(GC_onboard);
            GC_onboardtemp.add(GC_onboard.get(0));
            GC_onboardtemp.add(GC_onboard.get(2));
            GC_onboardtemp.add(GC_onboard.get(1));
            All_onboard.add(GC_onboardtemp);
            GC_onboardtemp = new ArrayList<Parcel>();
            GC_onboardtemp.add(GC_onboard.get(1));
            GC_onboardtemp.add(GC_onboard.get(0));
            GC_onboardtemp.add(GC_onboard.get(2));
            All_onboard.add(GC_onboardtemp);
            GC_onboardtemp = new ArrayList<Parcel>();
            GC_onboardtemp.add(GC_onboard.get(1));
            GC_onboardtemp.add(GC_onboard.get(2));
            GC_onboardtemp.add(GC_onboard.get(0));
            All_onboard.add(GC_onboardtemp);
            GC_onboardtemp = new ArrayList<Parcel>();
            GC_onboardtemp.add(GC_onboard.get(2));
            GC_onboardtemp.add(GC_onboard.get(0));
            GC_onboardtemp.add(GC_onboard.get(1));
            All_onboard.add(GC_onboardtemp);
            GC_onboardtemp = new ArrayList<Parcel>();
            GC_onboardtemp.add(GC_onboard.get(2));
            GC_onboardtemp.add(GC_onboard.get(1));
            GC_onboardtemp.add(GC_onboard.get(0));
            All_onboard.add(GC_onboardtemp);
        }
        return All_onboard;
    }
    /* Compare the Heuristic */
    public Benifit_Path<Double,ArrayList<Parcel>> compareHeuristic(Parcel BP_candidate, ArrayList<Parcel> BP_onboard, RoadModel rm) {
        // by rm.
        double T_C_cost;
        List<Point> path_temp = rm.getShortestPathTo(this,BP_candidate);
        Measure<Double, Length> This_to_Candidate;
        try{
            This_to_Candidate = rm.getDistanceOfPath(path_temp);
        } catch (IllegalArgumentException e) {
            if(!path_temp.isEmpty()){path_temp.remove(0);}
            This_to_Candidate = rm.getDistanceOfPath(path_temp);
        }

        T_C_cost = This_to_Candidate.getValue();
        ArrayList<Parcel> BP_newonboard = new ArrayList<Parcel> (BP_onboard);
        BP_newonboard.add(BP_candidate);
        double C_D_cost;
        //System.out.println("till2");
        Measure<Double, Length> Candidate_to_Destination = rm.getDistanceOfPath(rm.getShortestPathTo(BP_candidate,BP_candidate.getDeliveryLocation()));
        C_D_cost = Candidate_to_Destination.getValue();
        if(BP_onboard.size()==0){return new Benifit_Path<>(C_D_cost/2,BP_newonboard);}//this DC_cost/2 determine the logic: 1 means emtpy car first, 0 means same way car first
        //System.out.println("till1");
        double nopickupcost = pathCost(this,BP_onboard,rm);

        //System.out.println("till3");
        //System.out.println(getCombination(BP_newonboard));
        Benifit_Path<Double, ArrayList<Parcel>> pickup = getBestPath(BP_candidate,getCombination(BP_newonboard), rm);
        //System.out.println("till4");
        //System.out.println(T_C_cost+" + "+pickup.cost+" + "+nopickupcost+" + "+C_D_cost);
        if(T_C_cost+pickup.cost <=nopickupcost+C_D_cost) {
            //System.out.println("pickup");
            return new Benifit_Path<Double,ArrayList<Parcel>>(nopickupcost+C_D_cost-T_C_cost-pickup.cost,pickup.path);
        }else {
            //System.out.println("nopickup");
            return new Benifit_Path<Double,ArrayList<Parcel>>(0.0,BP_onboard);
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse){}

    @Override
    protected void tickImpl(TimeLapse time){
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();
        boolean pickupflag;
        boolean deliveryflag =false;
        //the heuristic can benifit from pick up extra pass.
        //Can be 0 as standard.
        //System.out.println("start of "+this+marked+marked_delay);
        //System.out.println("onboard: "+onboard+"   intenstion:   "+onboardwithintention);
        //System.out.println("candidate: "+candidate);
        //System.out.println("time:" +!time.hasTimeLeft());


        if(!time.hasTimeLeft()){
            return;
        }
        marked = marked_delay;

//                    !!! never step into this while
        if(marked.isPresent()&& !rm.containsObject(marked.get())  /*how to write mark*/){
//            System.out.println("processprocessprocessprocessprocessprocess1"+this);
            try {
                rm.getDistanceOfPath(rm.getShortestPathTo(this, station)).getValue();//check bus at a point
                marked = Optional.absent();
                marked_delay = Optional.absent();
            } catch (IllegalArgumentException e) {
                if (onboard.size() > 0) {
                    marked = Optional.absent();
                    marked_delay = Optional.absent();
                } else {
                    //get N pass
                    new_candidate_list = station.getNearestNPass(rm.getPosition(this), rm);
                    System.out.println(new_candidate_list);
                    //maintian the candidate list
                    //take candidates
                    candidate_flag_temp = new ArrayList<>();//indicating candidate check or not
                    candidate_list_temp = new ArrayList<>();
                    while (!new_candidate_list.isEmpty()) {
                        Parcel can =new_candidate_list.remove(0);
                        candidate_list_temp.add(can);
                        if(candidate_list.contains(can)){
                            candidate_flag_temp.add(candidate_flag.get(candidate_list.indexOf(can)));
                        }else {
                            candidate_flag_temp.add(false);
                        }
                    }
                    candidate_list = new ArrayList<>(candidate_list_temp);
                    candidate_flag = new ArrayList<>(candidate_flag_temp);

                    //take a candidate from list---still unchecked candidate
                    boolean unchecked_candidate = candidate_flag.contains(false);
                    if(unchecked_candidate == true){
                        candidate = Optional.fromNullable(candidate_list.get(candidate_flag.indexOf(false)));
                    }else{
                        candidate = Optional.absent();}
                    if(candidate.isPresent()) {
                        candidate_flag.set(candidate_list.indexOf(candidate.get()),false);
                        marked = candidate;
                        marked_delay = candidate;
                        onboardwithintention = new ArrayList<Parcel>();
                        onboardwithintention.add(candidate.get());
                    }else {
                        return;
                    }
                }
            }
        }
//        =============state main======================
        if(!marked_delay.isPresent() && onboard.size() < 3 && onboardnum < this.getCapacity()){
//            System.out.println("processprocessprocessprocessprocessprocess2"+this);
            //check still can take pass:find pass;delivery
            //station.getPassinRadius(rm.getPosition(this), rm);
            //get N pass
            //station.getPassinRadius(rm.getPosition(this),rm);
            new_candidate_list = station.getNearestNPass(rm.getPosition(this), rm);
            //maintian the candidate list
            candidate_flag_temp = new ArrayList<>();
            candidate_list_temp = new ArrayList<>();
            while (!new_candidate_list.isEmpty()) {
                Parcel can =new_candidate_list.remove(0);
                candidate_list_temp.add(can);
                if(candidate_list.contains(can)){
                    candidate_flag_temp.add(candidate_flag.get(candidate_list.indexOf(can)));
                }else {
                    candidate_flag_temp.add(false);
                }
            }
            candidate_list = new ArrayList<>(candidate_list_temp);
            candidate_flag = new ArrayList<>(candidate_flag_temp);
            //take a candidate from list---still unchecked candidate
            boolean unchecked_candidate = candidate_flag.contains(false);
            //System.out.println("Debug1:");
            //System.out.println(candidate_list);
            //System.out.println(candidate_flag);
            //System.out.println(candidate_flag.indexOf(true));
            if(unchecked_candidate == true){
                candidate = Optional.fromNullable(candidate_list.get(candidate_flag.indexOf(false)));
            }else{
            candidate = Optional.absent();}
            //old one
            //candidate = Optional.fromNullable(station.getNearestPassangerInfo(rm.getPosition(this), rm));//read a new candidate from center
//           ===============for later: get second nearest candidate, in station==============
            if (candidate.isPresent()&&(candidate.get().getNeededCapacity() + onboardnum > this.getCapacity())) {
                candidate_flag.set(candidate_list.indexOf(candidate.get()),true);
                candidate = Optional.absent();
            }
            if(candidate.isPresent()) {

                //System.out.println("Cand List: "+candidate_list + "Cand checked:  "+candidate_flag+ "  candi:  " + candidate);
                //================check if bus at a point is correct==================


                //System.out.println("good2:"+candidate.get()+"list:"+onboard);
                Benifit_Path<Double, ArrayList<Parcel>> pickup = compareHeuristic(candidate.get(), onboard, rm);
                //System.out.println("Heu:");
                //System.out.println(pickup);


                candidate_flag.set(candidate_list.indexOf(candidate.get()),true);
                //center compare the intention with the best intention now(in center's database).
                if (pickup.cost > 0) {
                    //System.out.println("sending intention++++++++++++++++");

                    List<Point> path_temp = rm.getShortestPathTo(this, candidate.get());
                    Double distance;
                    try{distance=rm.getDistanceOfPath(path_temp).getValue();}
                    catch (IllegalArgumentException e){
                        if(!path_temp.isEmpty()){path_temp.remove(0);}
                        distance=rm.getDistanceOfPath(path_temp).getValue();
                    }
                    pickupflag = station.
                            intention_pick(this, candidate.get(), pickup.cost, distance);
                    if (!pickupflag) {
                        marked_delay = Optional.absent();
                    } else {
                        marked_delay = candidate;
                        //System.out.println("Position1");
                        //System.out.println(marked_delay);
                        onboardwithintention = pickup.path;
                    }
                } else if (pickup.cost <= 0) {
                    marked_delay = Optional.absent();
                }


            }else{//stay
                 }
        }
//===========next state
        if(marked.isPresent() && rm.containsObject(marked.get()) )   {
            //System.out.println("processprocessprocessprocessprocessprocess3"+this);

            Benifit_Path<Double, ArrayList<Parcel>> pickup = compareHeuristic(candidate.get(), onboard, rm);
            //System.out.println("checking intention-----------------");
            List<Point> path_temp = rm.getShortestPathTo(this, candidate.get());
            Double distance;
            try{distance=rm.getDistanceOfPath(path_temp).getValue();}
            catch (IllegalArgumentException e){
                if(!path_temp.isEmpty()){path_temp.remove(0);}
                distance=rm.getDistanceOfPath(path_temp).getValue();
            }
            pickupflag = station.
                   intention_pick(this, candidate.get(), pickup.cost,distance);
            if(!pickupflag){
                marked = Optional.absent();
                marked_delay = Optional.absent();
            }

        }

        if(marked.isPresent()){
            //System.out.println("processprocessprocessprocessprocessprocess4"+this);
            rm.moveTo(this, marked.get(), time);
            if (rm.getPosition(this).equals(marked.get().getPickupLocation())) {
                onboard = onboardwithintention;
                onboardnum += marked.get().getNeededCapacity();
                //System.out.println("-++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+rm.containsObject(marked.get()));
                station.cleanpass(marked.get());
                pm.pickup(this, marked.get(), time);
                //System.out.println("-++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+rm.containsObject(marked.get()));
                //System.out.println("-++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+onboard);

                //marked = Optional.absent();
                marked_delay = Optional.absent();
            }
        }
        //System.out.println(!marked.isPresent() && !marked_delay.isPresent());
        if (!marked.isPresent() && !marked_delay.isPresent()&&onboard.size()>0) {
            //System.out.println("processprocessprocessprocessprocessprocess5"+this);
            rm.moveTo(this, onboard.get(0).getDeliveryLocation(), time);
            if(rm.getPosition(this).equals(onboard.get(0).getDeliveryLocation())) {
                onboardnum -= onboard.get(0).getNeededCapacity();
                //System.out.println("+-+++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+onboard);
                //System.out.println("+-+++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+onboardnum);
                pm.deliver(this, onboard.get(0), time);
                onboard.remove(0);
                for (int checked_cound=0;checked_cound<candidate_flag.size();checked_cound++){
                    candidate_flag.set(checked_cound,false);
                }
                //System.out.println("+-+++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+onboard);
            }

        }
    }

}
