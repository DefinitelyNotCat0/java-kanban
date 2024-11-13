package kanban.manager;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static final String testFileName = "FileBackedTaskManagerTest";
    private static final String fileExtension = ".csv";
    private static File testFile;

    @BeforeEach
    void setUp() {
        try {
            testFile = File.createTempFile(testFileName, fileExtension);
            taskManager = new FileBackedTaskManager(testFile);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException.getMessage());
        }
    }

    @Test
    void createFileAndLoadFromIt() {
        // Слздаем задачи (и заполнеям файл)
        createTestTasks();
        // Создам новй менеджер без задач
        FileBackedTaskManager taskManager2 = new FileBackedTaskManager(null);
        assertEquals(0, taskManager2.getTaskList().size());
        assertEquals(0, taskManager2.getSubTaskList().size());
        assertEquals(0, taskManager2.getEpicList().size());

        // Заполняем новый менеджер задачами из файла, который был создан первым менеджером
        taskManager2 = FileBackedTaskManager.loadFromFile(testFile);
        assertEquals(2, taskManager2.getTaskList().size());
        assertEquals(3, taskManager2.getSubTaskList().size());
        assertEquals(2, taskManager2.getEpicList().size());

        // Проверяем, что задачи первого менеджера совпали с задачами второго
        assertArrayEquals(taskManager.getTaskList().toArray(), taskManager2.getTaskList().toArray());

        // Проверяем, что подзадачи первого менеджера совпали с подзадачами второго
        assertArrayEquals(taskManager.getSubTaskList().toArray(), taskManager2.getSubTaskList().toArray());

        // Проверяем, что эпики первого менеджера совпали с эпиками второго
        assertArrayEquals(taskManager.getEpicList().toArray(), taskManager2.getEpicList().toArray());
    }

    @Test
    void loadFromNonExistantFile() {
        assertThrows(RuntimeException.class, () -> FileBackedTaskManager.loadFromFile(
                new File("file_that_not_exists.csv")));
    }
}