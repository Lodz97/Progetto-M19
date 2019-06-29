package TestJunit;
import static org.junit.jupiter.api.Assertions.assertEquals;

import main.Manager.ExitManager;
import main.Manager.Manager;
import main.Manager.EntryManager;
import org.junit.jupiter.api.Test;

public class exitManagerTest {

    Manager m = new Manager();
    EntryManager em = new EntryManager(m);
    ExitManager ee = new ExitManager(m);
    splitString split = new splitString();

    public void makePark() {
        m.makeFloors(5, 200);
        m.setSpacesSubdivision(500);
    }

    @Test
    public void validExitSub() {
        makePark();
        em.entrySub("00000000", "Mensile");
        // set paySub
        m.getDriver("00000000").setPaidSub(true);
        System.out.println("paySub:" + m.getDriver("00000000").getPaySub());
        assertEquals("exitOk--Uscita abbonamento avvenuta con successo 00000000",
                ee.exit("00000000"));
    }

    @Test
    public void setInParkTest() {
        makePark();
        em.entrySub("11111111", "Mensile");
        // set paySub
        m.getDriver("11111111").setPaidSub(true);
        System.out.println("paySub:" + m.getDriver("11111111").getPaySub());
        ee.exit("11111111");
        assertEquals(false, m.getDriver("11111111").getInPark());
    }

    @Test
    public void notBuySub() {
        makePark();
        em.entrySub("00000000", "Mensile");
        // no set pay Sub
        System.out.println("paySub:" + m.getDriver("00000000").getPaySub());
        assertEquals("exitNo--L'abbonamento non è pagato, si prega di tornare alle casse.",
                ee.exit("00000000"));
    }

    @Test
    public void notSubInPark() {
        makePark();
        em.entrySub("00000000", "Mensile");
        // set pay Sub
        m.getDriver("00000000").setPaidSub(true);
        //setto l'uscita manualmente
        m.getDriver("00000000").setInPark(false);
        //provo ad uscire con una targa che all'interno del parcheggio non c'è
        assertEquals("exitNo--Uscita fallita, l'abbonato non è nel parcheggio 00000000",
                ee.exit("00000000"));
    }

    @Test
    public void ticketNotPay() {
        makePark();
        em.entryTicket("00000000");
        assertEquals("exitNo--Ticket non pagato, torna in cassa!", ee.exit("00000000"));
    }
}
