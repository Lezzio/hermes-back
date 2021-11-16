package fr.insalyon.messenger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.messenger.net.model.AuthenticationMessage;
import fr.insalyon.messenger.net.model.GroupMessage;
import fr.insalyon.messenger.net.model.Message;
import fr.insalyon.messenger.net.model.PrivateMessage;
import fr.insalyon.messenger.net.serializer.RuntimeTypeAdapterFactory;

import java.util.Date;

public class Main {

    //POC of the runtime type adapter factory
    private static final TypeToken<Message> messageTypeToken = new TypeToken<>() {};
    private static final RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory
            .of(Message.class, "type")
            .registerSubtype(GroupMessage.class)
            .registerSubtype(PrivateMessage.class)
            .registerSubtype(AuthenticationMessage.class);

    public static void main(String ... args) {
        System.out.println("Ok");

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(typeFactory)
                .create();

        Message msg = new AuthenticationMessage("aguigal", "test", new Date());
        var str = gson.toJson(msg, messageTypeToken.getType());
        System.out.println("String json = " + str);
        Message unserializedMessage = gson.fromJson(str, messageTypeToken.getType());
        System.out.println("unserialized = " + unserializedMessage + " class = " + unserializedMessage.getClass().getName());
    }

}