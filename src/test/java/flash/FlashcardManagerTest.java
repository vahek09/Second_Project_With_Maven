package flash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlashcardManagerTest {
    private FlashcardManager flashcardManager;
    private LogManager logManager;
    private Scanner scannerMock;


    @BeforeEach
    void setUp() {
        flashcardManager = new FlashcardManager();
        logManager = Mockito.mock(LogManager.class);
        scannerMock = Mockito.mock(Scanner.class);

    }

    @Test
    @DisplayName("Add Term and Definition Test")
    void testAddCard() {
        //given
        Scanner scanner = new Scanner("Term1\nDefinition1");
        //when
        flashcardManager.addCard(scanner, logManager);
        //then
        assertTrue(flashcardManager.flashcards.containsKey("Term1"));
        assertEquals("Definition1", flashcardManager.flashcards.get("Term1").getDefinition());
    }

    @Test
    @DisplayName("Add Card with Empty Strings Test")
    void testAddCard_EmptyStrings() {
        // Given
        Scanner scanner = new Scanner("\n");
        // When
        flashcardManager.addCard(scanner, logManager);
        // Then
        verify(logManager).logAndPrint("The card:");
        verify(logManager).logAndPrint("The term cannot be empty. Please enter a valid term.");
        assertTrue(flashcardManager.flashcards.isEmpty());
    }


    @Test
    @DisplayName("Duplicate Term Test")
    void testAddCard_DuplicateTerm() {
        //given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        Scanner scanner = new Scanner("Term1\nDefinition2");

        //when
        flashcardManager.addCard(scanner, logManager);

        //then
        verify(logManager).logAndPrint("The card \"Term1\" already exists.");
        assertEquals(1, flashcardManager.flashcards.size());
        assertEquals("Definition1", flashcardManager.flashcards.get("Term1").getDefinition());
    }

    @Test
    @DisplayName("Duplicate Definition Test")
    void testAddCard_DuplicateDefinition() {
        //given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        Scanner scanner = new Scanner("Term2\nDefinition1");

        //when
        flashcardManager.addCard(scanner, logManager);

        //then
        InOrder inOrder = inOrder(logManager);
        inOrder.verify(logManager).logAndPrint("The card:");
        inOrder.verify(logManager).addToLog("Term2");
        inOrder.verify(logManager).logAndPrint("The definition of the card:");
        inOrder.verify(logManager).addToLog("Definition1");
        inOrder.verify(logManager).logAndPrint("The definition \"Definition1\" already exists.");
        assertEquals(1, flashcardManager.flashcards.size());
    }

    @Test
    @DisplayName("Test Adding a Card with an Empty Definition")
    void testAddCard_EmptyDefinition() {
        // Given
        Scanner scanner = mock(Scanner.class);

        when(scanner.nextLine())
                .thenReturn("TestTerm")
                .thenReturn("");

        // When
        flashcardManager.addCard(scanner, logManager);

        // Then
        // Verify that logManager was used correctly
        verify(logManager).logAndPrint("The card:");
        verify(logManager).addToLog("TestTerm");
        verify(logManager).logAndPrint("The definition of the card:");
        verify(logManager).addToLog("");  // The empty definition
        verify(logManager).logAndPrint("The definition cannot be empty. Please enter a valid term.");

        // Ensure that the card was not added to the flashcards map
        assertFalse(flashcardManager.flashcards.containsKey("TestTerm"), "The card should not be added when the definition is empty.");
    }

    @Test
    @DisplayName("Remove existing Card Test")
    void removeCard() {
        //given
        flashcardManager.flashcards.put("TermToRemove1", new Flashcard("DefinitionToRemove1"));
        flashcardManager.flashcards.put("TermToRemove2", new Flashcard("DefinitionToRemove2"));
        flashcardManager.flashcards.put("TermToRemove3", new Flashcard("DefinitionToRemove3"));
        Scanner scanner = new Scanner("TermToRemove2");

        //when
        flashcardManager.removeCard(scanner, logManager);

        //then
        verify(logManager).logAndPrint("Which card?");
        verify(logManager).addToLog("TermToRemove2");
        verify(logManager).logAndPrint("The card has been removed.");
        assertFalse(flashcardManager.flashcards.containsKey("TermToRemove2"));
        assertEquals(2, flashcardManager.flashcards.size());
    }

    @Test
    @DisplayName("Remove Card that doesn't exist Test")
    void removeCard_NoExistCard() {
        //given
        Scanner scanner = new Scanner("Term");
        //when
        flashcardManager.removeCard(scanner, logManager);
        //then
        verify(logManager).logAndPrint("Which card?");
        verify(logManager).addToLog("Term");
        verify(logManager).logAndPrint("Can't remove \"Term\": there is no such card.");
        assertTrue(flashcardManager.flashcards.isEmpty());
    }

    @Test
    @DisplayName("Resetting Stats Test")
    void testResetStats() {
        //given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        flashcardManager.flashcards.put("Term2", new Flashcard("Definition2"));
        flashcardManager.flashcards.get("Term1").setMistakes(5);
        flashcardManager.flashcards.get("Term2").setMistakes(3);

        //when
        flashcardManager.resetStats(logManager);

        //then
        assertEquals(0, flashcardManager.flashcards.get("Term1").getMistakes());
        assertEquals(0, flashcardManager.flashcards.get("Term2").getMistakes());
        verify(logManager).logAndPrint("Card statistics have been reset.");
    }

    @Test
    @DisplayName("Having Multiple Hardest Cards Test")
    public void testPrintHardestCard() {
        // given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        flashcardManager.flashcards.put("Term2", new Flashcard("Definition2"));
        flashcardManager.flashcards.put("Term3", new Flashcard("Definition3"));
        flashcardManager.flashcards.get("Term1").setMistakes(5);
        flashcardManager.flashcards.get("Term2").setMistakes(5);
        flashcardManager.flashcards.get("Term3").setMistakes(5);

        // when
        flashcardManager.printHardestCard(logManager);

        // then
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(logManager, times(1)).logAndPrint(argumentCaptor.capture());
        assertEquals("The hardest cards are \"Term1\", \"Term2\", \"Term3\". You have 5 errors answering them.", argumentCaptor.getValue());
    }

    @Test
    @DisplayName("Single Hardest Card Test")
    void testPrintHardestCard_SingleHardestCard() {
        // Given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        flashcardManager.flashcards.put("Term2", new Flashcard("Definition2"));
        flashcardManager.flashcards.put("Term3", new Flashcard("Definition3"));
        flashcardManager.flashcards.get("Term1").setMistakes(5);
        flashcardManager.flashcards.get("Term2").setMistakes(2);
        flashcardManager.flashcards.get("Term3").setMistakes(3);

        //when
        flashcardManager.printHardestCard(logManager);

        //then
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(logManager, times(1)).logAndPrint(argumentCaptor.capture());
        assertEquals("The hardest card is \"Term1\". You have 5 errors answering it.", argumentCaptor.getValue());
    }

    @Test
    @DisplayName("No Cards with Mistakes Test")
    void testPrintHardestCard_NoMistakes() {

        //when
        flashcardManager.printHardestCard(logManager);

        //then
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(logManager, times(1)).logAndPrint(argumentCaptor.capture());
        assertEquals("There are no cards with errors.", argumentCaptor.getValue());
    }

    @Test
    @DisplayName("Card Import with arguments")
    void testCheckArgsWithImport() throws Exception {
        // given
        String[] args = {"-import", "cards.txt"};
        FlashcardManager spyManager = Mockito.spy(flashcardManager);

        doNothing().when(spyManager).importCards(eq("cards.txt"), any(LogManager.class));

        // when
        spyManager.checkArgs(args, false, logManager);

        // then
        verify(spyManager, times(1)).importCards("cards.txt", logManager);

    }

    @Test
    @DisplayName("Card Export with arguments")
    void testCheckArgsWithExport() throws Exception {
        // given
        FlashcardManager spyManager = Mockito.spy(flashcardManager);
        String[] args = {"-export", "cards.txt"};

        doNothing().when(spyManager).exportCards(eq("cards.txt"), any(LogManager.class));

        // when
        spyManager.checkArgs(args, true, logManager);

        // then
        verify(spyManager, times(1)).exportCards("cards.txt", logManager);
    }

    @Test
    @DisplayName("importCards should handle file not found scenario")
    void testImportCards_FileNotFound() {
        //given
        String nonExistentFileName = "Non_Existent_File.txt";

        //when
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> {
            flashcardManager.importCards(nonExistentFileName, logManager);
        });

        //then
        verify(logManager).logAndPrint("File name:");
        verify(logManager).addToLog(nonExistentFileName);
        verify(logManager).logAndPrint("File not found.");
        verifyNoMoreInteractions(logManager);

        assertEquals("File not found: " + nonExistentFileName, exception.getMessage());
    }


    @Test
    @DisplayName("Import Cards File Not Found Second")
    void testImportCards_FileNotFoundSecond() throws Exception {
        // given
        String fileName = "nonExistentFile.txt";
        Scanner scanner = new Scanner(fileName);
        FlashcardManager spyManager = Mockito.spy(flashcardManager);

        doThrow(new Exception("File not found")).when(spyManager).importCards(eq(fileName), any(LogManager.class));

        // when
        Exception exception = assertThrows(Exception.class, () -> {
            spyManager.importCards(scanner, logManager);
        });

        // then
        assertEquals("File not found", exception.getMessage());
        verify(logManager).logAndPrint("File name:");
        verify(logManager).addToLog(fileName);
        verify(logManager).logAndPrint("File not found: " + fileName);
    }


    @Test
    @DisplayName("Import Cards from File Success")
    void testImportCards_Success() throws Exception {
        // given
        String fileName = "testCards.txt";
        File tempFile = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("Term1:Definition1:2\n");
            writer.write("Term2:Definition2:3\n");
        }

        // when
        flashcardManager.importCards(fileName, logManager);

        // then
        assertEquals(2, flashcardManager.flashcards.size());
        assertEquals("Definition1", flashcardManager.flashcards.get("Term1").getDefinition());
        assertEquals(2, flashcardManager.flashcards.get("Term1").getMistakes());
        assertEquals("Definition2", flashcardManager.flashcards.get("Term2").getDefinition());
        assertEquals(3, flashcardManager.flashcards.get("Term2").getMistakes());
        verify(logManager).logAndPrint("File name:");
        verify(logManager).addToLog(fileName);
        verify(logManager).logAndPrint("2 cards have been loaded.");

        tempFile.delete();
    }

    @Test
    @DisplayName("Import Cards with Invalid Content")
    void testImportCards_InvalidContent() throws Exception {
        // given
        String fileName = "invalidContent.txt";
        File tempFile = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("Term1:Definition1\n");
            writer.write("Term2:Definition2:3:Extra\n");
        }

        // when
       Exception thrown = assertThrows(Exception.class, () -> {
            flashcardManager.importCards(fileName, logManager);
        });
        assertTrue(thrown.getMessage().contains("Invalid line format"));
        verify(logManager).logAndPrint("File name:");
        verify(logManager).addToLog(fileName);
        verify(logManager).logAndPrint("Error writing to the file.");

        //then
        tempFile.delete();
    }

    @Test
    @DisplayName("Import Cards from Empty File")
    void testImportCards_EmptyFile() throws Exception {
        // given
        String fileName = "emptyFile.txt";
        File tempFile = new File(fileName);
        tempFile.createNewFile();

        // when
        flashcardManager.importCards(fileName, logManager);

        // then
        assertTrue(flashcardManager.flashcards.isEmpty());
        verify(logManager).logAndPrint("File name:");
        verify(logManager).addToLog(fileName);
        verify(logManager).logAndPrint("0 cards have been loaded.");

        // Clean up
        tempFile.delete();
    }

    @Test
    @DisplayName("Export Cards Successfully")
    public void testExportCardsSuccessfully() throws FileNotFoundException {
        // given
        File tempFile = new File("exported_cards_test.txt");
        tempFile.deleteOnExit();

        Scanner scanner = new Scanner("exported_cards_test.txt");

        // when
        flashcardManager.exportCards(scanner, logManager);

        // then
        verify(logManager,times(2)).logAndPrint("File name:");

        verify(logManager,times(2)).addToLog("exported_cards_test.txt");

        assertTrue(tempFile.exists(), "The file should be created");
    }



    @Test
    @DisplayName("Export Cards File Not Found Test")
    void testExportCards_FileNotFound() throws Exception {
        // given
        String fileName = "nonExistentFile.txt";
        Scanner scanner = new Scanner(fileName);
        FlashcardManager spyManager = Mockito.spy(flashcardManager);

        doThrow(new FileNotFoundException("File not found")).when(spyManager).exportCards(eq(fileName), any(LogManager.class));

        // when
        assertThrows(FileNotFoundException.class, () -> {
            spyManager.exportCards(scanner, logManager);
        });
        // then
        verify(logManager).logAndPrint("File name:");
        verify(logManager).addToLog(fileName);
        verify(logManager).logAndPrint("File not found: " + fileName);
    }

    @Test
    @DisplayName("Handle IOException During Export")
    void testExportCards_IOException() {
        //given
        String invalidFilename = "/invalid_path/test_flashcards_export.txt";

        //when
        IOException thrown = assertThrows(IOException.class, () -> {
            flashcardManager.exportCards(invalidFilename, logManager);
        });

        //then
        verify(logManager).logAndPrint("File name:");
        verify(logManager).addToLog(invalidFilename);
        verify(logManager).logAndPrint("Error writing to the file.");
    }

    @Test
    @DisplayName("Export Cards with Empty Flashcards Map")
    void testExportCards_EmptyFlashcardsMap() throws IOException {
        //given
        String filename = "test_empty_flashcards_export.txt";

        //when
        flashcardManager.exportCards(filename, logManager);

        //then
        File file = new File(filename);
        assertTrue(file.exists());

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            assertNull(reader.readLine());  // File should be empty
        }

        verify(logManager).logAndPrint("File name:");
        verify(logManager).addToLog(filename);
        verify(logManager).logAndPrint("0 cards have been saved.");

        // Clean up
        file.delete();
    }

    @Test
    @DisplayName("Test Successful Export Cards")
    void testExportCards_Success() throws IOException {
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        flashcardManager.flashcards.put("Term2", new Flashcard("Definition2"));
        // Given
        String filename = "testfile.txt";

        // When
        flashcardManager.exportCards(filename, logManager);

        // Then
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(logManager, atLeast(2)).logAndPrint(logCaptor.capture());

        // Capture the log messages
        List<String> capturedLogs = logCaptor.getAllValues();

        // Expected log messages
        assertEquals("File name:", capturedLogs.get(0));
        assertEquals("2 cards have been saved.", capturedLogs.get(1));
    }

    @Test
    @DisplayName("Ask Definitions with No Matching Definition Test")
    void testAskDefinitions_NoMatchingDefinition() {

        // given
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));
        Scanner scanner = new Scanner("1\nWrongDefinition");

        // when
        flashcardManager.askDefinitions(scanner, logManager);

        // then
        InOrder inOrder = inOrder(logManager);
        inOrder.verify(logManager).logAndPrint("How many times to ask?");
        inOrder.verify(logManager).addToLog("1");
        inOrder.verify(logManager).logAndPrint("Print the definition of \"Term1\":");
        inOrder.verify(logManager).addToLog("WrongDefinition");
        inOrder.verify(logManager).logAndPrint("Wrong. The right answer is \"Definition1\".");
        assertEquals(1, flashcardManager.flashcards.get("Term1").getMistakes());
    }

    @Test
    @DisplayName("askDefinitions should handle invalid number format input")
    void testAskDefinitions_InvalidNumberFormat() {
        //given
        String invalidInput = "not_a_number";
        Scanner scanner = new Scanner(invalidInput);

        //when
        flashcardManager.askDefinitions(scanner, logManager);

        //then
        verify(logManager).logAndPrint("How many times to ask?");
        verify(logManager).logAndPrint("Invalid number format: " + invalidInput);
        verifyNoMoreInteractions(logManager);
    }

    @Test
    @DisplayName("Valid Number of Times to Ask")
    void testAskDefinitions_CorrectAnswer() {
        // Given: One flashcard with the correct answer provided
        flashcardManager.flashcards.put("Term1", new Flashcard("Definition1"));

        when(scannerMock.nextLine())
                .thenReturn("1")    // Number of times to ask
                .thenReturn("Definition1");  // User's correct answer

        // When: Calling askDefinitions
        flashcardManager.askDefinitions(scannerMock, logManager);

        // Then: Verify logManager output
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(logManager, times(3)).logAndPrint(argumentCaptor.capture());

        List<String> expectedOutputs = Arrays.asList(
                "How many times to ask?",
                "Print the definition of \"Term1\":",
                "Correct!"
        );

        // Check only the captured outputs matching our expectations
        assertEquals(expectedOutputs, argumentCaptor.getAllValues().subList(0, expectedOutputs.size()));

        // Verify the mistakes count is zero as the answer was correct
        assertEquals(0, flashcardManager.flashcards.get("Term1").mistakes);
    }
}