package project.service;

import project.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLoggingService {

    public void log(User user, String text, Enum status) {
        String firstName = user.getFirstName();
        String userId = Long.toString(user.getId());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        System.out.println(dateFormat.format(date) +" ["+ status +"] "+ firstName + " (id = "+userId+") "+text);

    }
}
