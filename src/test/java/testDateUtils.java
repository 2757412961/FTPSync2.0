import Utils.DateUtils;
import org.junit.Test;

public class testDateUtils {
    @Test
    public void testGETYYYMMDD() {

        System.out.println(DateUtils.getYYYYMMDD());

        System.out.println(DateUtils.getToday().getTime());
        System.out.println(DateUtils.get24hBefore().getTime());
        System.out.println(DateUtils.getToday00().getTime());

        System.out.println(DateUtils.getToday().before(DateUtils.getToday()));
        System.out.println(DateUtils.get24hBefore().before(DateUtils.getToday()));
        System.out.println(DateUtils.getToday00().before(DateUtils.get24hBefore()));
    }

}
