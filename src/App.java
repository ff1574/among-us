import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class App {

    void writeXML() {
        // Example how to write
        AmongUsSettings amongUsSettings = new AmongUsSettings("localhost", 1234, 40);

        try {

            File file = new File("settings.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(AmongUsSettings.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(amongUsSettings, file);
            jaxbMarshaller.marshal(amongUsSettings, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    void readXML() {
        File xmlFile = new File("settings.xml");

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(AmongUsSettings.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            AmongUsSettings employee = (AmongUsSettings) jaxbUnmarshaller.unmarshal(xmlFile);
            System.out.println(employee);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new App().writeXML();
        new App().readXML();

    }

}
