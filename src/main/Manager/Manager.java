package main.Manager;

import Exceptions.NotEmptyFloorException;
import Exceptions.SubdivisionException;
import GUIs.ManagerGUI;
import main.Manager.DataBase.DataBaseAdapter;
import main.Manager.DataBase.TextDataBaseAdapter;
import main.Peripherals.Cash.Cash;
import main.Peripherals.Columns.Column;
import main.Peripherals.Observer;
import net.Server;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

@SuppressWarnings("Duplicates")
//CHANGED METHODS: FIRST TEST: positive
public class Manager
{
    private double monthlyCost=1, semestralCost, annualCost, extraCost;

    private int peripheralId = 0;

    private Server server;

    private DataBaseAdapter db;

    private EntryManager entryMan;
    private ExitManager exitMan;

    private ArrayList<Floor> floorsList;
    private int freeSpacesTot, freeSpacesSubTot, freeSpacesTicketTot;
    private int freeSpacesSubNow, freeSpacesTicketNow;
    private double tariff;
    private ArrayList<Driver> drivers, subDrivers;
    private ArrayList<Cash> cashList;
    private ArrayList<Column> columnList;
    // paymantAnalytics variables
    private int entryToT;
    private double DAYS=365, MONTH=12;
    private HashMap<String, Command> commands;

    //aggiungo l'arraylist degli abbonamenti
    //private ArrayList<Subscription> sublist;  Ora sono in subDrivers

    //aggiungo deltaTime
    private int deltaTimePaid;  //In minuti


    public Manager(int port)
    {
        this.floorsList = new ArrayList<>();
        this.freeSpacesTot = 0;
        this.freeSpacesSubTot = 0;
        this.freeSpacesTicketTot = 0;
        this.freeSpacesSubNow = 0;
        this.freeSpacesTicketNow = 0;
        this.drivers = new ArrayList<>();
        this.subDrivers = new ArrayList<>();
        this.entryToT = 0;
        this.columnList = new ArrayList<>();

        this.db = new TextDataBaseAdapter("./db");

        this.entryMan = new EntryManager(this);
        this.exitMan = new ExitManager(this);

        createCommands();

        Manager m = this;
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new ManagerGUI(m);
            }
        });

        this.server = new Server(port, this);
        this.server.startServer();

        //arraylist abbonamenti
        //this.sublist = new ArrayList<>();
    }

    public static void main(String[] args)
    {
        if (args.length < 1) return;
        new Manager(Integer.parseInt(args[0]));
    }

    public void createCommands()
    {
        commands = new HashMap<>();
        commands.put("getId", (String[] args) -> "id--" + peripheralId());
        commands.put("help", (String[] args) -> {
            System.out.println("Richiesta assistenza periferica " + args[1]);
            return "helpComing--Assitenza in arrivo, attendi.";
        });
        commands.put("entry", (String[] args) -> entryMan.entryTicket(args[1]));
        commands.put("entrySub", (String[] args) -> entryMan.entrySub(args[1], args[2]));
        commands.put("getTariff", (String[] args) -> "tariff--" + getTariff());
        commands.put("getSubTariffs", (String[] args) -> "subTariffs--" + getSubTariffs());
        commands.put("exit", (String[] args) -> exitMan.exit(args[1]));
        commands.put("driverInfo", (String[] args) -> getDriverClientInfo(args[1]));
    }

    public String executeCommand(String[] args)
    {
        String s = "";
        try
        {
            s = commands.get(args[0]).execute(args);
        }
        catch(NullPointerException ex)
        {
            System.out.println("Comando errato");
        }
        return s;
    }

    // ho cambiato il metodo perchè non settava il numero di posti liberi dei piani
    public void makeFloors(int numFloors, int numSpaces)
    {
        for(int i=0; i<numFloors; i++)
        {
            Floor floor = new Floor(floorsList.size(), numSpaces);
            floorsList.add(floor);
        }
        setFreeSpacesTot();
    }

    public void removeFloor(int rm)
    {
        Floor toBeRemoved = new Floor(-1, -1);
        for (Floor f : floorsList)
        {
            if(f.getId() == rm)
            {
                if (f.getCountCarIn() != 0)
                {
                    throw new NotEmptyFloorException("Non puoi rimuovere un piano non vuoto.");
                }
                //NB mai rimuovere oggetti in un foreach
                toBeRemoved = f;
            }
        }
        floorsList.remove(toBeRemoved);
        changeFloorId();
        setFreeSpacesTot();
    }

    boolean checkDeltaTime(GregorianCalendar dataDriverPaid)
    {
        GregorianCalendar dataNow = new GregorianCalendar();
        if(dataDriverPaid != null)
        {
            dataDriverPaid.add(Calendar.MINUTE, deltaTimePaid);
        }
        return dataNow.before(dataDriverPaid);

        /*double DeltaTime = dataNow.getTimeInMillis() - dataDriver.getTimeInMillis();
        DeltaTime = DeltaTime/(1000*60*60); //risalgo ai minuti
        return DeltaTime;*/ //VISTO: POSSIAMO ANCHE ELIMINARE IL DeltaTime se sei d'accordo
    }

    // ho cambiato il metodo da ''private'' a ''public'' perchè non potevo settare dal main il numero dei posti per gli abbonati
    public void setSpacesSubdivision(int sub)
    {
        if(sub <= freeSpacesTot)
        {
            if(freeSpacesTicketNow <= freeSpacesTot - sub && freeSpacesSubNow <= sub)
            {
                freeSpacesSubTot = sub;
                freeSpacesTicketTot = freeSpacesTot - sub;
            }
            else
            {
                throw new SubdivisionException("Non ci sono abbastanza posti");
            }
        }
        else
        {
            throw new SubdivisionException("Non ci sono abbastanza posti");
        }
    }

    private void setFreeSpacesTot()  //Modificare non dovrebbe restituire nulla
    {
        int i = 0;
        for(Floor f : floorsList)
        {
            i += f.getFreeSpace();
        }
        freeSpacesTot = i;
        freeSpacesTicketTot = freeSpacesTot - freeSpacesSubTot;
        //Gestico caso in cui eliminando i piani ho piu posti in abbonamneto che posti liberi
        if (freeSpacesTicketTot < 0)
        {
            freeSpacesSubTot = freeSpacesTicketTot;
            freeSpacesTicketTot = 0;
        }
    }

    private void changeFloorId()
    {
        for(int i=0;i<floorsList.size();i++)
        {
            floorsList.get(i).setId(i);
        }
    }

    // analisi ingressi e incassi
    public void Analytics()
    {

        // NumberFormat arrotonda un double per eccesso alle ultime due cifre decimali  0.41666666 --> 0.417
        NumberFormat nf = new DecimalFormat("0.000");
        double meanDay = (double)entryToT / DAYS;
        double meanMonth = (double)entryToT / MONTH;
        double meanPayDay = meanDay*tariff;
        double meanPayMth = meanMonth*tariff;

        System.out.println("MEDIA INGRESSI: \nGioralieri:  " + nf.format(meanDay) + "\t" + "Mensili:  "+nf.format(meanMonth));
        System.out.println("**********************************");
        System.out.println("MEDIA INCASSI: \nGioralieri:  " + nf.format(meanPayDay) + "\t" + "Mensili:  "+nf.format(meanPayMth));
    }

    String printTickt(String carId)
    {
        String s = "";
        s += "IDTicket:   " + carId;
        for(Driver d : drivers)
        {
            if(d.getCarId().equals(carId)){
                s+= ", ora Ingresso:  " + d.getTimeIn().toZonedDateTime().toString(); // toZonedDateTime converte nel nuovo formato di tempo di java 1.8
            }
        }
        return s;
    }

    //*********************************** metodi 'check' per abbonamento****************************
    boolean checkDateSub(String carID)
    {
        GregorianCalendar dataNow = new GregorianCalendar();
        boolean check = false;
        for(Driver d : subDrivers)
        {
            if(d.getCarId().equals(carID))
            {
                if(dataNow.after(d.getDateFinishOfSub()))  //Pattern protected variations
                {
                    check = false;
                }
                else
                {
                    check = true;
                }
            }
        }
        return  check;
    }

    boolean checkSubOrTicket(String carID)
    {
        boolean check = false;
        for(Driver d : subDrivers)
        {
            if(d.getCarId().equals(carID))
            {
                check = true;
            }
        }
        for (Driver d : drivers)
        {
            if(d.getCarId().equals(carID))
            {
                check = true;
            }
        }



        return check;
    }

    boolean checkInPark(String cardID)
    {
        boolean check = false;
        for (Driver d : subDrivers){
            if(d.getCarId().equals(cardID))
            {
                if(d.getInPark())
                {
                    check = true;
                }
            }
        }
        return check;
    }

    boolean checkCarId(String carId)
    {
        if(carId.length() == 8)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
// ************** fine metodi check abbonamento ************************************

//****************** metodo check in park per tickets *******************************

    boolean checkTicket(String carID)
    {
        boolean check = false;
        for (Driver d : drivers)
        {
            if(d.getCarId().equals(carID))
            {
                check = true;
            }
        }

        return  check;
    }

    //*******************************************

    public String getDriverClientInfo(String carID)
    {
        StringBuilder sb = new StringBuilder();
        boolean check = false;
        for(Driver d : subDrivers)
        {
            if(d.getCarId().equals(carID))
            {
                sb.append(d.infoClient());
                check = true;
            }
        }
        for (Driver d : drivers)
        {
            if(d.getCarId().equals(carID))
            {
                sb.append(d.infoClient());
                check = true;
            }
        }
        if(check)
        {
            return sb.toString();
        }
        else
        {
            return "info--0";
        }
    }

    //****************** fine metodo check in park per tickets *******************************

    void removeSub(String carID)
    {
        Driver toBeRemoved = new Driver("");
        for(Driver d : subDrivers)
        {
            if(d.getCarId().equals(carID))
            {
                toBeRemoved = d;
            }
        }
        subDrivers.remove(toBeRemoved);
    }

    void randomEntry()
    {
        Random r = new Random();
        int i;
        // Impedisco che si superi il numero massimo di utenti per piano
        do
        {
            i = r.nextInt(floorsList.size());
        }while(floorsList.get(i).getCountCarIn() >= floorsList.get(i).getFreeSpace());
        floorsList.get(i).addCar();
    }

    void randomExit()
    {
        Random r = new Random();
        int i;
        // Impedisco che i posti occupati vadano in negativo
        do
        {
            i = r.nextInt(floorsList.size());
        }while(floorsList.get(i).getCountCarIn() <= 0);
        floorsList.get(i).deleteCar();
    }

    private void addObserver(List<Observer> list, Observer obs)
    {
        list.add(obs);
    }

    private void notifyColumns()
    {
        for (Column c : columnList)
        {
            c.notifyObs();
        }
    }

    private String peripheralId()
    {
        peripheralId++;
        return "ID_" + peripheralId;
    }


    //Get and set
    public void setTariff(int tariff)
    {
        this.tariff = tariff;
        server.updatePeripherals("getTariff");
    }

    public void setDeltaTimePaid(int deltaTimePaid)
    {
        this.deltaTimePaid = deltaTimePaid;
        server.updatePeripherals("getTariff");
    }

    public Driver getDriver(String carId)
    {
        for(Driver d : drivers)
        {
            if(d.getCarId().equals(carId))
            {
                return d;
            }
        }
        for (Driver d : subDrivers)
        {
            if(d.getCarId().equals(carId))
            {
                return d;
            }
        }
        return null;
    }

    public String getFloorsInfo()
    {
        StringBuilder sb = new StringBuilder();
        for (Floor f : floorsList)
        {
            sb.append(f.getFloorInfo());
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getDriversInfo()
    {
        StringBuilder sb = new StringBuilder();
        for (Driver d : drivers)
        {
            sb.append(d.getDriverInfo());
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getSubDriversInfo()
    {
        StringBuilder sb = new StringBuilder();
        for (Driver d : subDrivers)
        {
            sb.append(d.getDriverInfo());
            sb.append("\n");
        }
        return sb.toString();
    }

    public ArrayList<Floor> getFloorsList()
    {
        return floorsList;
    }

    public double getTariff()
    {
        return tariff;
    }

    public ArrayList<Double> getSubTariffs()
    {
        ArrayList<Double> t = new ArrayList<>();
        t.add(monthlyCost);
        t.add(semestralCost);
        t.add(annualCost);
        return t;
    }

    public DataBaseAdapter getDb() {
        return db;
    }

    public int getFreeSpacesTot()
    {
        return freeSpacesTot;
    }

    public int getFreeSpacesSubTot()
    {
        return freeSpacesSubTot;
    }

    public int getFreeSpacesSubNow() {
        return freeSpacesSubNow;
    }

    public void setFreeSpacesSubNow(int freeSpacesSubNow) {
        this.freeSpacesSubNow = freeSpacesSubNow;
    }

    public int getFreeSpacesTicketTot()
    {
        return freeSpacesTicketTot;
    }

    public int getFreeSpacesTicketNow()
    {
        return freeSpacesTicketNow;
    }

    public void setFreeSpacesTicketNow(int freeSpacesTicketNow) {
        this.freeSpacesTicketNow = freeSpacesTicketNow;
    }

    public ArrayList<Driver> getDrivers()
    {
        return drivers;
    }

    public ArrayList<Driver> getSubDrivers()
    {
        return subDrivers;
    }

    public int getDeltaTimePaid() {
        return deltaTimePaid;
    }

    public double getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(double monthlyCost)
    {
        this.monthlyCost = monthlyCost;
        server.updatePeripherals("getSubTariffs");
    }

    public double getSemestralCost() {
        return semestralCost;
    }

    public void setSemestralCost(double semestralCost)
    {
        this.semestralCost = semestralCost;
        server.updatePeripherals("getSubTariffs");
    }

    public double getAnnualCost() {
        return annualCost;
    }

    public void setAnnualCost(double annualCost)
    {
        this.annualCost = annualCost;
        server.updatePeripherals("getSubTariffs");
    }

    public double getExtraCost() {
        return extraCost;
    }

    public void setExtraCost(double extraCost) {
        this.extraCost = extraCost;
    }

    public int getEntryToT() {
        return entryToT;
    }

    public void setEntryToT(int entryToT) {
        this.entryToT = entryToT;
    }
}