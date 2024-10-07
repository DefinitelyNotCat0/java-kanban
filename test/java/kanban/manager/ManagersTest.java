package kanban.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagersTest {

    @Test
    void getDefault() {
        assertTrue(Managers.getDefault() instanceof InMemoryTaskManager);
    }

    @Test
    void getDefaultHistory() {
        assertTrue(Managers.getDefaultHistory() instanceof InMemoryHistoryManager);
    }
}