package TestJunit;
import static org.junit.jupiter.api.Assertions.assertEquals;

import main.Parking.Exit;

import main.Parking.Entrance;
import main.Parking.Parking;
import org.junit.jupiter.api.Test;

public class exitTest {

    Parking m = new Parking();
    Entrance em = new Entrance(m);
    Exit ee = new Exit(m);
    splitString split = new splitString();

    public void makePark() {
        m.makeFloors(5, 200);
        m.setSpacesSubdivision(500);
    }

    @Test
    public void validExitSub() {
        makePark();
        em.entrySub("0000000", "Mensile");
        //Set paySub
        m.getDriver("0000000").setPaidSub(true);
        System.out.println("paySub:" + m.getDriver("0000000").getPaySub());
        assertEquals("exitOk--Uscita abbonamento avvenuta con successo 0000000",
                ee.exit("0000000"));
    }

    @Test
    public void setInParkTest() {
        makePark();
        em.entrySub("1111111", "Mensile");
        //Set paySub
        m.getDriver("1111111").setPaidSub(true);
        System.out.println("paySub:" + m.getDriver("1111111").getPaySub());
        ee.exit("1111111");
        assertEquals(false, m.getDriver("1111111").getInPark());
    }

    @Test
    public void notBuySub() {
        makePark();
        em.entrySub("0000000", "Mensile");
        //No set pay Sub
        System.out.println("paySub:" + m.getDriver("0000000").getPaySub());
        assertEquals("exitNo--L'abbonamento non è pagato, si prega di tornare alle casse.",
                ee.exit("0000000"));
    }

    @Test
    public void notSubInPark() {
        makePark();
        em.entrySub("0000000", "Mensile");
        //Set pay Sub
        m.getDriver("0000000").setPaidSub(true);
        //Setto l'uscita manualmente
        m.getDriver("0000000").setInPark(false);
        //Provo ad uscire con una targa che all'interno del parcheggio non c'è
        assertEquals("exitNo--Uscita fallita, l'abbonato non è nel parcheggio 0000000",
                ee.exit("0000000"));
    }

    @Test
    public void ticketNotPay() {
        makePark();
        em.entryTicket("0000000");
        assertEquals("exitNo--Ticket non pagato, torna in cassa!", ee.exit("0000000"));
    }
}

