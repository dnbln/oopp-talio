package client;

import client.scenes.AddTagCtrl;
import client.scenes.AdminLoginCtrl;
import client.scenes.BoardListCtrl;
import client.scenes.BoardOverviewCtrl;
import client.scenes.CardDetailsCtrl;
import client.scenes.CardListViewCtrl;
import client.scenes.CustomizeCtrl;
import client.scenes.CustomizeListCtrl;
import client.scenes.MainCtrl;
import client.scenes.ServerConnectCtrl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * The main application.
 */
public class Main extends Application {

    private static final Injector INJECTOR = Guice.createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    /**
     * The main method that launches the application.
     *
     * @param args Default parameter of main.
     */
    public static void main(final String[] args) {
        launch();
    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     * NOTE: This method is called on the JavaFX Application Thread.
     *
     * @param primaryStage The primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     */
    @Override
    public void start(final Stage primaryStage) {

        var overview = FXML.load(BoardOverviewCtrl.class, "client", "scenes", "BoardOverview.fxml");
        AbstractModule myNewModule = new MyCardListViewModule();
        Injector cardListViewInjector = INJECTOR.createChildInjector(myNewModule);
        var serverConnect = FXML.load(ServerConnectCtrl.class, "client", "scenes", "ServerConnect.fxml");
        var adminLogin = FXML.load(AdminLoginCtrl.class, "client", "scenes", "AdminLogin.fxml");
        var cardDetails = FXML.load(CardDetailsCtrl.class, "client", "scenes", "CardDetails.fxml");
        var boardList = FXML.load(BoardListCtrl.class, "client", "scenes", "BoardList.fxml");
        var addTag = FXML.load(AddTagCtrl.class, "client", "scenes", "AddTag.fxml");
        var customize = FXML.load(CustomizeCtrl.class, "client", "scenes", "Customize.fxml");
        var customizeList = FXML.load(CustomizeListCtrl.class, "client", "scenes", "CustomizeList.fxml");

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, overview, serverConnect, adminLogin, cardDetails, boardList, addTag,
                customize, customizeList, cardListViewInjector);
        primaryStage.setOnCloseRequest(event -> serverConnect.getKey().stopLongPollingThread());
    }

    private static class MyCardListViewModule extends AbstractModule {
        @Override
        protected void configure() {
            this.bind(Pair.class).annotatedWith(Names.named("CardListViewPair"))
                    .toProvider(() -> FXML.load(CardListViewCtrl.class, "client", "scenes", "CardListView.fxml"));
        }
    }
}
