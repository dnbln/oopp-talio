package client.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class WorkspaceTest {
    private Workspace workspace;

    @BeforeEach
    void setup() {
        workspace = new Workspace();
    }

    @Test
    void testConstructor() {
        Assertions.assertNotNull(workspace);
    }

    @Test
    void testGetBoardIds() {
        List<Long> boards = workspace.getBoardIds();
        Assertions.assertEquals(boards, new ArrayList<>());
    }

    @Test
    void testSetBoardIds() {
        List<Long> boards = new ArrayList<>();
        boards.add(1L);
        workspace.setBoardIds(boards);
        Assertions.assertEquals(boards, workspace.getBoardIds());
    }

    @Test
    void testAddBoardId() {
        List<Long> boards = new ArrayList<>();
        workspace.setBoardIds(boards);
        workspace.addBoardId(1L);
        Assertions.assertTrue(workspace.getBoardIds().contains(1L));
    }

    @Test
    void testRemoveBoardId() {
        List<Long> boards = new ArrayList<>();
        workspace.setBoardIds(boards);
        workspace.addBoardId(1L);
        Assertions.assertTrue(workspace.getBoardIds().contains(1L));
        workspace.removeBoardId(1L);
        Assertions.assertFalse(workspace.getBoardIds().contains(1L));
    }

    @Test
    void testClearBoardIds() {
        List<Long> boards = new ArrayList<>();
        workspace.setBoardIds(boards);
        workspace.addBoardId(1L);
        workspace.addBoardId(2L);
        workspace.addBoardId(3L);
        workspace.clearBoardIds();
        Assertions.assertTrue(workspace.getBoardIds().isEmpty());
    }

    @Test
    void cleanupBoardIds() {
        List<Long> boards = new ArrayList<>();
        workspace.setBoardIds(boards);
        workspace.addBoardId(1L);
        workspace.addBoardId(2L);
        workspace.addBoardId(3L);
        List<Long> retain = new ArrayList<>();
        retain.add(1L);
        retain.add(2L);
        workspace.cleanupBoardIds(retain);
        Assertions.assertEquals(retain, workspace.getBoardIds());
    }

    @Test
    void testToString() {
        List<Long> boards = new ArrayList<>();
        workspace.setBoardIds(boards);
        Assertions.assertEquals("client.utils.Workspace{boardIds=[]}", workspace.toString());
    }

    @Test
    void testEquals() {
        Workspace other = new Workspace();
        List<Long> boards = new ArrayList<>();
        workspace.setBoardIds(boards);
        other.setBoardIds(boards);
        Assertions.assertEquals(workspace, other);
    }

    @Test
    void testNotEquals() {
        Workspace other = new Workspace();
        List<Long> boards = new ArrayList<>();
        List<Long> otherBoards = new ArrayList<>();
        workspace.setBoardIds(boards);
        other.setBoardIds(otherBoards);
        workspace.addBoardId(1L);
        other.addBoardId(2L);
        Assertions.assertNotEquals(workspace, other);
    }

    @Test
    void testHashCode() {
        Workspace other = new Workspace();
        List<Long> boards = new ArrayList<>();
        boards.add(1L);
        workspace.setBoardIds(boards);
        other.setBoardIds(boards);
        Assertions.assertEquals(workspace.hashCode(), other.hashCode());
    }
}
