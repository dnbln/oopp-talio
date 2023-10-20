package client.scenes;

import client.utils.ServerUtilsInterface;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import commons.Card;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MainCtrlTest {

    @Mock
    private Stage primaryStage;

    @Mock
    private BoardOverviewCtrl overviewCtrl;

    @Mock
    private ServerConnectCtrl serverConnectCtrl;

    @Mock
    private AdminLoginCtrl adminLoginCtrl;

    @Mock
    private CardDetailsCtrl cardDetailsCtrl;

    @Mock
    private BoardListCtrl boardListCtrl;

    @Mock
    private AddTagCtrl addTagCtrl;

    @Mock
    private ServerUtilsInterface server;

    @Mock
    private CustomizeCtrl customizeCtrl;

    @Mock
    private CustomizeListCtrl customizeListCtrl;

    @InjectMocks
    private MainCtrl mainCtrl;

    /**
     * This is run before each separate test.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Injector injector = Guice.createInjector(new MainCtrlTestModule());
        this.mainCtrl = injector.getInstance(MainCtrl.class);
    }

    private class MainCtrlTestModule extends AbstractModule {
        @Override
        protected void configure() {
            this.bind(MainCtrl.class).toInstance(MainCtrlTest.this.mainCtrl);
            this.bind(Stage.class).toInstance(MainCtrlTest.this.primaryStage);
            this.bind(BoardOverviewCtrl.class).toInstance(MainCtrlTest.this.overviewCtrl);
            this.bind(ServerConnectCtrl.class).toInstance(MainCtrlTest.this.serverConnectCtrl);
            this.bind(AdminLoginCtrl.class).toInstance(MainCtrlTest.this.adminLoginCtrl);
            this.bind(CardDetailsCtrl.class).toInstance(MainCtrlTest.this.cardDetailsCtrl);
            this.bind(BoardListCtrl.class).toInstance(MainCtrlTest.this.boardListCtrl);
            this.bind(AddTagCtrl.class).toInstance(MainCtrlTest.this.addTagCtrl);
            this.bind(ServerUtilsInterface.class).toInstance(MainCtrlTest.this.server);
            bind(CustomizeCtrl.class).toInstance(customizeCtrl);
            bind(CustomizeListCtrl.class).toInstance(customizeListCtrl);
        }
    }

    @Test
    void initializeTest() {
        try (MockedConstruction<Scene> ignored = Mockito.mockConstruction(Scene.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {

            MainCtrl aSpy = Mockito.spy(this.mainCtrl);
            Mockito.doNothing().when(aSpy).showConnect();

            Mockito.doNothing().when(this.primaryStage).setMinHeight(ArgumentMatchers.anyDouble());
            Mockito.doNothing().when(this.primaryStage).setMinWidth(ArgumentMatchers.anyDouble());

            AbstractModule newModule = new AbstractModule() {
                @Override
                protected void configure() {
                    super.configure();
                }
            };

            Injector injector = Guice.createInjector(newModule);

            aSpy.initialize(this.primaryStage,
                    new Pair<>(this.overviewCtrl, null),
                    new Pair<>(this.serverConnectCtrl, null),
                    new Pair<>(this.adminLoginCtrl, null),
                    new Pair<>(this.cardDetailsCtrl, null),
                    new Pair<>(this.boardListCtrl, null),
                    new Pair<>(this.addTagCtrl, null),
                    new Pair<>(customizeCtrl, null),
                    new Pair<>(customizeListCtrl, null),
                    injector.createChildInjector(newModule));
            Mockito.verify(aSpy, Mockito.times(1)).showConnect();
        }
    }

    @Test
    void showOverviewTest() {
        MainCtrl aSpy = Mockito.spy(this.mainCtrl);
        Mockito.doNothing().when(aSpy).showOverviewWithBoardId("");

        aSpy.showOverview();

        Mockito.verify(aSpy, Mockito.times(1))
                .showOverviewWithBoardId("");

    }

    @Test
    void showOverviewWithBoardIdIsShowingTest() {
        String boardIdString = "title";

        Mockito.doReturn(true).when(this.primaryStage)
                .isShowing();
        Mockito.doNothing().when(this.primaryStage)
                .setTitle("Talio - Board %s".formatted(boardIdString));
        Mockito.doNothing().when(this.primaryStage)
                .setScene(ArgumentMatchers.any());
        Mockito.doNothing().when(this.overviewCtrl)
                .reloadBoard(Mockito.any());

        this.mainCtrl.showOverviewWithBoardId(boardIdString);

        Mockito.verify(this.primaryStage, Mockito.times(1))
                .isShowing();
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .setTitle("Talio - Board %s".formatted(boardIdString));
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .setScene(ArgumentMatchers.any());
        Mockito.verify(this.overviewCtrl, Mockito.times(1))
                .reloadBoard(Mockito.any());

    }

    @Test
    void showOverviewWithBoardIdIsNotShowingTest() {
        String boardIdString = "title";

        Mockito.doReturn(false).when(this.primaryStage)
                .isShowing();
        Mockito.doNothing().when(this.primaryStage)
                .show();
        Mockito.doNothing().when(this.primaryStage)
                .setTitle("Talio - Board %s".formatted(boardIdString));
        Mockito.doNothing().when(this.primaryStage)
                .setScene(ArgumentMatchers.any());
        Mockito.doNothing().when(this.overviewCtrl)
                .reloadBoard(Mockito.any());

        this.mainCtrl.showOverviewWithBoardId(boardIdString);

        Mockito.verify(this.primaryStage, Mockito.times(1))
                .isShowing();
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .show();
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .setTitle("Talio - Board %s".formatted(boardIdString));
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .setScene(ArgumentMatchers.any());
        Mockito.verify(this.overviewCtrl, Mockito.times(1))
                .reloadBoard(Mockito.any());

    }

    @Test
    void showConnectIsShowingTest() {
        try (MockedConstruction<Stage> ignored = Mockito.mockConstruction(Stage.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {

            Mockito.doReturn(true).when(this.primaryStage)
                    .isShowing();
            Mockito.doNothing().when(this.primaryStage)
                    .hide();

            this.mainCtrl.showConnect();

            Mockito.verify(this.primaryStage, Mockito.times(1))
                    .isShowing();
            Mockito.verify(this.primaryStage, Mockito.times(1))
                    .hide();
        }
    }

    @Test
    void showConnectIsNotShowingTest() {
        try (MockedConstruction<Stage> ignored = Mockito.mockConstruction(Stage.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {

            Mockito.doReturn(false).when(this.primaryStage)
                    .isShowing();

            this.mainCtrl.showConnect();

            Mockito.verify(this.primaryStage, Mockito.times(1))
                    .isShowing();
            Mockito.verify(this.primaryStage, Mockito.times(0))
                    .hide();
        }
    }

    @Test
    void showCardDetailsFromListIDTest() {
        long listId = 1L;
        MainCtrl aSpy = Mockito.spy(this.mainCtrl);
        Mockito.doNothing().when(aSpy)
                .showCardDetails(listId, null);

        aSpy.showCardDetails(listId);

        Mockito.verify(aSpy, Mockito.times(1))
                .showCardDetails(listId, null);
    }

    @Test
    void showCardDetailsFromCardTest() {
        Card card = new Card();
        MainCtrl aSpy = Mockito.spy(this.mainCtrl);
        Mockito.doNothing().when(aSpy)
                .showCardDetails(0L, card);

        aSpy.showCardDetails(card);

        Mockito.verify(aSpy, Mockito.times(1))
                .showCardDetails(0L, card);
    }

    @Test
    void showCardDetailsFromListIDAndCardNotZeroAndNotNullTest() {
        try (MockedConstruction<Stage> ignored = Mockito.mockConstruction(Stage.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {

            long listId = 1L;
            Card card = new Card();

            Mockito.doNothing().when(this.cardDetailsCtrl)
                    .setListId(listId);
            Mockito.doNothing().when(this.cardDetailsCtrl)
                    .setFields(card);

            this.mainCtrl.showCardDetails(listId, card);

            Mockito.verify(this.cardDetailsCtrl, Mockito.times(1))
                    .setListId(listId);
            Mockito.verify(this.cardDetailsCtrl, Mockito.times(1))
                    .setFields(card);
        }
    }

    @Test
    void showCardDetailsFromListIDAndCardZeroAndNotNullTest() {
        try (MockedConstruction<Stage> ignored = Mockito.mockConstruction(Stage.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {

            long listId = 0L;
            Card card = new Card();

            Mockito.doNothing().when(this.cardDetailsCtrl)
                    .setFields(card);

            this.mainCtrl.showCardDetails(listId, card);

            Mockito.verify(this.cardDetailsCtrl, Mockito.times(0))
                    .setListId(listId);
            Mockito.verify(this.cardDetailsCtrl, Mockito.times(1))
                    .setFields(card);
        }
    }

    @Test
    void showCardDetailsFromListIDAndCardNotZeroAndNullTest() {
        try (MockedConstruction<Stage> ignored = Mockito.mockConstruction(Stage.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {

            long listId = 1L;
            Card card = null;

            Mockito.doNothing().when(this.cardDetailsCtrl)
                    .setListId(listId);

            this.mainCtrl.showCardDetails(listId, card);

            Mockito.verify(this.cardDetailsCtrl, Mockito.times(1))
                    .setListId(listId);
            Mockito.verify(this.cardDetailsCtrl, Mockito.times(0))
                    .setFields(card);
        }
    }

    @Test
    void showCardDetailsFromListIDAndCardZeroAndNullTest() {
        try (MockedConstruction<Stage> ignored = Mockito.mockConstruction(Stage.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {

            long listId = 0L;
            Card card = null;

            this.mainCtrl.showCardDetails(listId, card);

            Mockito.verify(this.cardDetailsCtrl, Mockito.times(0))
                    .setListId(listId);
            Mockito.verify(this.cardDetailsCtrl, Mockito.times(0))
                    .setFields(card);
        }
    }


    @Test
    void showBoardListIsShowingTest() {
        Mockito.doReturn(true).when(this.primaryStage)
                .isShowing();
        Mockito.doNothing().when(this.primaryStage)
                .setTitle("Board List");
        Mockito.doNothing().when(this.primaryStage)
                .setScene(ArgumentMatchers.any());
        Mockito.doNothing().when(this.boardListCtrl)
                .initData();

        this.mainCtrl.showBoardList();

        Mockito.verify(this.primaryStage, Mockito.times(1))
                .isShowing();
        Mockito.verify(this.primaryStage, Mockito.times(0))
                .show();
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .setTitle("Board List");
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .setScene(ArgumentMatchers.any());
        Mockito.verify(this.boardListCtrl, Mockito.times(1))
                .initData();
    }

    @Test
    void showBoardListIsNotShowingTest() {
        Mockito.doReturn(false).when(this.primaryStage)
                .isShowing();
        Mockito.doNothing().when(this.primaryStage)
                .show();
        Mockito.doNothing().when(this.primaryStage)
                .setTitle("Board List");
        Mockito.doNothing().when(this.primaryStage)
                .setScene(ArgumentMatchers.any());
        Mockito.doNothing().when(this.boardListCtrl)
                .initData();

        this.mainCtrl.showBoardList();

        Mockito.verify(this.primaryStage, Mockito.times(1))
                .isShowing();
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .show();
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .setTitle("Board List");
        Mockito.verify(this.primaryStage, Mockito.times(1))
                .setScene(ArgumentMatchers.any());
        Mockito.verify(this.boardListCtrl, Mockito.times(1))
                .initData();
    }
}
