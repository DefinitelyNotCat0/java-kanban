package kanban.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ManagersTest {

    @Test
    void getDefault() {
        assertInstanceOf(InMemoryTaskManager.class, Managers.getDefault());
    }

    @Test
    void getDefaultHistory() {
        assertInstanceOf(InMemoryHistoryManager.class, Managers.getDefaultHistory());
    }
}