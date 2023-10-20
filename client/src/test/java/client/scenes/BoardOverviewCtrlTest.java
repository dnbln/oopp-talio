package client.scenes;

import client.utils.NoListThereException;
import client.utils.ServerUtilsInterface;
import commons.Board;
import commons.CardList;
import commons.events.ServerToClientEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class BoardOverviewCtrlTest {

    @Mock
    private ServerUtilsInterface server;

    @Mock
    private MainCtrl mainCtrl;

    @Spy
    @InjectMocks
    private BoardOverviewCtrl boardOverviewCtrl;

    @Test
    void firstTest() {
        // Should always work.
    }

    @Test
    void setFocusTest() {
        ServerToClientEvent eventMock = Mockito.mock(ServerToClientEvent.class);

        boardOverviewCtrl.setFocus(eventMock);
    }

    @Test
    void addListTest() {
        CardList cardList = Mockito.mock(CardList.class);
        Mockito.when(cardList.getId()).thenReturn(1L);

        Mockito.when(server.addList()).thenReturn(cardList);
        Mockito.doNothing().when(server)
                .setListTitle(Mockito.anyLong(), Mockito.anyString());

        boardOverviewCtrl.addList();

        Mockito.verify(server, Mockito.times(1))
                .addList();
        Mockito.verify(cardList, Mockito.times(1))
                .getId();
        Mockito.verify(server, Mockito.times(1))
                .setListTitle(Mockito.anyLong(), Mockito.anyString());
    }

    @Test
    void backToServerConnectTest() {
        try (MockedConstruction<Alert> ignored = Mockito.mockConstruction(Alert.class,
                Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {
            try (MockedConstruction<ButtonType> ignored2 = Mockito.mockConstruction(ButtonType.class,
                    Mockito.withSettings().defaultAnswer(Answers.RETURNS_MOCKS))) {

                boardOverviewCtrl.backToServerConnect();
            }
        }
    }

    @Test
    void changeBoardTest() {
        try {
            Mockito.doNothing().when(server)
                    .unsubscribe();
            Mockito.doNothing().when(mainCtrl)
                    .showBoardList();

            boardOverviewCtrl.changeBoard();

            Mockito.verify(server, Mockito.times(1))
                    .unsubscribe();
            Mockito.verify(mainCtrl, Mockito.times(1))
                    .showBoardList();
        } catch (Exception e) {
            System.out.println("Error");
        }
    }

    @Test
    void handleKeyPressedQuestionTest() {
        javafx.scene.input.KeyEvent keyEventMock = Mockito.mock(javafx.scene.input.KeyEvent.class);
        Mockito.doReturn("?").when(keyEventMock)
                .getText();

        Mockito.doNothing().when(mainCtrl)
                .showHelpAlert();

        try {
            boardOverviewCtrl.handleKeyPressed(keyEventMock);
        } catch (Exception e) {
            System.out.println("Error");
        }

        Mockito.verify(keyEventMock, Mockito.times(1))
                .getText();
        Mockito.verify(mainCtrl, Mockito.times(1))
                .showHelpAlert();
    }

    @Test
    void handleKeyPressedKeyCodeHTest() {
        javafx.scene.input.KeyEvent keyEventMock = Mockito.mock(javafx.scene.input.KeyEvent.class);
        Mockito.doReturn("No").when(keyEventMock)
                .getText();
        Mockito.doReturn(KeyCode.H).when(keyEventMock)
                .getCode();

        Mockito.doNothing().when(mainCtrl)
                .showHelpAlert();

        try {
            boardOverviewCtrl.handleKeyPressed(keyEventMock);
        } catch (Exception e) {
            System.out.println("Error");
        }

        Mockito.verify(keyEventMock, Mockito.times(1))
                .getText();
        Mockito.verify(keyEventMock, Mockito.times(1))
                .getCode();
        Mockito.verify(mainCtrl, Mockito.times(1))
                .showHelpAlert();
    }

    @Test
    void handleKeyPressedKeyCodeSLASHTest() {
        javafx.scene.input.KeyEvent keyEventMock = Mockito.mock(javafx.scene.input.KeyEvent.class);
        Mockito.doReturn("No").when(keyEventMock)
                .getText();
        Mockito.doReturn(KeyCode.SLASH).when(keyEventMock)
                .getCode();

        Mockito.doNothing().when(mainCtrl)
                .showHelpAlert();

        try {
            boardOverviewCtrl.handleKeyPressed(keyEventMock);
        } catch (Exception e) {
            System.out.println("Error");
        }

        Mockito.verify(keyEventMock, Mockito.times(1))
                .getText();
        Mockito.verify(keyEventMock, Mockito.times(2))
                .getCode();
        Mockito.verify(mainCtrl, Mockito.times(1))
                .showHelpAlert();
    }

    @Test
    void handleKeyPressedKeyCodeWTest() throws Exception {
        javafx.scene.input.KeyEvent keyEventMock = Mockito.mock(javafx.scene.input.KeyEvent.class);
        Mockito.doReturn("No").when(keyEventMock)
                .getText();
        Mockito.doReturn(KeyCode.W).when(keyEventMock)
                .getCode();

        Mockito.doNothing().when(boardOverviewCtrl)
                .changeBoard();

        try {
            boardOverviewCtrl.handleKeyPressed(keyEventMock);
        } catch (Exception e) {
            System.out.println("Error");
        }

        Mockito.verify(keyEventMock, Mockito.times(1))
                .getText();
        Mockito.verify(keyEventMock, Mockito.times(3))
                .getCode();
        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .changeBoard();
    }

    @Test
    void handleKeyPressedKeyCodeDTest() {
        javafx.scene.input.KeyEvent keyEventMock = Mockito.mock(javafx.scene.input.KeyEvent.class);
        Mockito.doReturn("No").when(keyEventMock)
                .getText();
        Mockito.doReturn(KeyCode.D).when(keyEventMock)
                .getCode();

        Mockito.doNothing().when(boardOverviewCtrl)
                .backToServerConnect();

        try {
            boardOverviewCtrl.handleKeyPressed(keyEventMock);
        } catch (Exception e) {
            System.out.println("Error");
        }

        Mockito.verify(keyEventMock, Mockito.times(1))
                .getText();
        Mockito.verify(keyEventMock, Mockito.times(4))
                .getCode();
        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .backToServerConnect();
    }

    @Test
    void handleKeyPressedKeyCodeATest() {
        javafx.scene.input.KeyEvent keyEventMock = Mockito.mock(javafx.scene.input.KeyEvent.class);
        Mockito.doReturn("No").when(keyEventMock)
                .getText();
        Mockito.doReturn(KeyCode.A).when(keyEventMock)
                .getCode();

        Mockito.doNothing().when(boardOverviewCtrl)
                .addList();

        try {
            boardOverviewCtrl.handleKeyPressed(keyEventMock);
        } catch (Exception e) {
            System.out.println("Error");
        }

        Mockito.verify(keyEventMock, Mockito.times(1))
                .getText();
        Mockito.verify(keyEventMock, Mockito.times(5))
                .getCode();
        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .addList();
    }

    @Test
    void handleKeyPressedKeyCodeTTest() {
        javafx.scene.input.KeyEvent keyEventMock = Mockito.mock(javafx.scene.input.KeyEvent.class);
        Mockito.doReturn("No").when(keyEventMock)
                .getText();
        Mockito.doReturn(KeyCode.T).when(keyEventMock)
                .getCode();

        Mockito.doNothing().when(boardOverviewCtrl)
                .showTagMenu();

        try {
            boardOverviewCtrl.handleKeyPressed(keyEventMock);
        } catch (Exception e) {
            System.out.println("Error");
        }

        Mockito.verify(keyEventMock, Mockito.times(1))
                .getText();
        Mockito.verify(keyEventMock, Mockito.times(6))
                .getCode();
        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .showTagMenu();
    }

    @Test
    void handleKeyPressedKeyCodeCTest() {
        javafx.scene.input.KeyEvent keyEventMock = Mockito.mock(javafx.scene.input.KeyEvent.class);
        Mockito.doReturn("No").when(keyEventMock)
                .getText();
        Mockito.doReturn(KeyCode.C).when(keyEventMock)
                .getCode();

        Mockito.doNothing().when(boardOverviewCtrl)
                .showCustomizeMenu(boardOverviewCtrl);

        try {
            boardOverviewCtrl.handleKeyPressed(keyEventMock);
        } catch (Exception e) {
            System.out.println("Error");
        }

        Mockito.verify(keyEventMock, Mockito.times(1))
                .getText();
        Mockito.verify(keyEventMock, Mockito.times(7))
                .getCode();
        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .showCustomizeMenu(Mockito.any(BoardOverviewCtrl.class));
    }

    @Test
    void getLeftListMinusOneTest() throws NoListThereException {
        Mockito.doReturn(-1).when(boardOverviewCtrl)
                .getCardListIndex(null);

        NoListThereException exception = Assertions
                .assertThrows(NoListThereException.class, () -> {
                    boardOverviewCtrl.getLeftList(null);
        });

        Assertions.assertEquals("There is no list to the left of this list.",
                exception.getMessage());

        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .getCardListIndex(null);
    }

    @Test
    void getLeftListZeroTest() throws NoListThereException {
        Mockito.doReturn(0).when(boardOverviewCtrl)
                .getCardListIndex(null);

        NoListThereException exception = Assertions
                .assertThrows(NoListThereException.class, () -> {
                    boardOverviewCtrl.getLeftList(null);
                });

        Assertions.assertEquals("There is no list to the left of this list.",
                exception.getMessage());

        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .getCardListIndex(null);
    }

    @Test
    void getLeftListNonMinusOneNonZeroTest() throws NoListThereException {
        Mockito.doReturn(2).when(boardOverviewCtrl)
                .getCardListIndex(null);

        Board boardMock = Mockito.mock(Board.class);
        Mockito.doReturn(boardMock).when(server)
                .getBoard();

        List<CardList> cardListMock = Mockito.mock(List.class);
        Mockito.doReturn(cardListMock).when(boardMock)
                .getCardLists();

        Mockito.doReturn(null).when(cardListMock)
                .get(Mockito.eq(1));

        boardOverviewCtrl.getLeftList(null);


        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .getCardListIndex(null);
        Mockito.verify(server, Mockito.times(1))
                .getBoard();
        Mockito.verify(boardMock, Mockito.times(1))
                .getCardLists();
        Mockito.verify(cardListMock, Mockito.times(1))
                .get(Mockito.eq(1));
    }

    @Test
    void getRightListMinusOneTest() throws NoListThereException {
        Mockito.doReturn(-1).when(boardOverviewCtrl)
                .getCardListIndex(null);

        NoListThereException exception = Assertions
                .assertThrows(NoListThereException.class, () -> {
                    boardOverviewCtrl.getRightList(null);
                });

        Assertions.assertEquals("There is no list to the right of this list.",
                exception.getMessage());

        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .getCardListIndex(null);
    }

    @Test
    void getRightListZeroTest() {
        Mockito.doReturn(0).when(boardOverviewCtrl)
                .getCardListIndex(null);

        Board boardMock = Mockito.mock(Board.class);
        Mockito.doReturn(boardMock).when(server)
                .getBoard();

        List<CardList> cardListMock = Mockito.mock(List.class);
        Mockito.doReturn(cardListMock).when(boardMock)
                .getCardLists();

        Mockito.doReturn(1).when(cardListMock)
                .size();

        NoListThereException exception = Assertions
                .assertThrows(NoListThereException.class, () -> {
                    boardOverviewCtrl.getRightList(null);
                });

        Assertions.assertEquals("There is no list to the right of this list.",
                exception.getMessage());

        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .getCardListIndex(null);
        Mockito.verify(server, Mockito.times(1))
                .getBoard();
        Mockito.verify(boardMock, Mockito.times(1))
                .getCardLists();
        Mockito.verify(cardListMock, Mockito.times(1))
                .size();
    }

    @Test
    void getRightListNonMinusOneNonZeroTest() throws NoListThereException {
        Mockito.doReturn(2).when(boardOverviewCtrl)
                .getCardListIndex(null);

        Board boardMock = Mockito.mock(Board.class);
        Mockito.doReturn(boardMock).when(server)
                .getBoard();

        List<CardList> cardListMock = Mockito.mock(List.class);
        Mockito.doReturn(cardListMock).when(boardMock)
                .getCardLists();

        Mockito.doReturn(1).when(cardListMock)
                .size();

        Mockito.doReturn(null).when(cardListMock)
                .get(Mockito.eq(3));

        boardOverviewCtrl.getRightList(null);


        Mockito.verify(boardOverviewCtrl, Mockito.times(1))
                .getCardListIndex(null);
        Mockito.verify(server, Mockito.times(2))
                .getBoard();
        Mockito.verify(boardMock, Mockito.times(2))
                .getCardLists();
        Mockito.verify(cardListMock, Mockito.times(1))
                .size();
        Mockito.verify(cardListMock, Mockito.times(1))
                .get(Mockito.eq(3));
    }

    @Test
    void showCustomizeMenu() {
        Mockito.doNothing().when(mainCtrl)
                .showCustomize(Mockito.eq(null));

        boardOverviewCtrl.showCustomizeMenu(null);

        Mockito.verify(mainCtrl, Mockito.times(1))
                .showCustomize(Mockito.eq(null));
    }

}
