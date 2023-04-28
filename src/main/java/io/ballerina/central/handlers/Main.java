package io.ballerina.central.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.ballerina.central.handlers.exceptions.BalaNotFoundException;
import io.ballerina.central.handlers.exceptions.BuildException;
import io.ballerina.central.handlers.models.trigger.Trigger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, BuildException, BalaNotFoundException {

//        Handler1 handler1 = new Handler1();
//        JsonObject handle = handler1.handle("https://balahandlerprem9663.blob.core.windows.net/bala-store/ballerinax-trigger.slack-any-0.6.6.bala");
//        JsonArray triggers = handle.getAsJsonArray("triggers");
//        Gson gson = new Gson();
//        Type listType = new TypeToken<List<Trigger>>(){}.getType();
//        List<Trigger> triggerList = gson.fromJson(triggers, listType);
//        System.out.println(triggerList.get(0).name);
    }
}
