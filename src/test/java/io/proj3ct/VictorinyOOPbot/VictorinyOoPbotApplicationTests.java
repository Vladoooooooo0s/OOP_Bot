package io.proj3ct.VictorinyOOPbot;

import io.proj3ct.VictorinyOOPbot.config.BotConfig;
import io.proj3ct.VictorinyOOPbot.model.Category;
import io.proj3ct.VictorinyOOPbot.model.GameSession;
import io.proj3ct.VictorinyOOPbot.model.UserRepository;
import io.proj3ct.VictorinyOOPbot.model.User;
import io.proj3ct.VictorinyOOPbot.service.AnswersService;
import io.proj3ct.VictorinyOOPbot.service.CategoryService;
import io.proj3ct.VictorinyOOPbot.service.QuizApiService;
import io.proj3ct.VictorinyOOPbot.service.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class VictorinyOoPbotApplicationTests {

	@Test
	void contextLoads() {
	}

	@Mock
	private BotConfig botConfig;

	@InjectMocks
	private TelegramBot telegramBot;

	@Mock
	private QuizApiService quizApiService;

	@Mock
	private CategoryService categoryService;

	@Mock
	private AnswersService answersService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private Map<Long, GameSession> userSessions;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		botConfig = mock(BotConfig.class);

		telegramBot = new TelegramBot(botConfig);

		injectMock("categoryService", categoryService);
		injectMock("answersService", answersService);
		injectMock("userSessions", userSessions);
	}

	private void injectMock(String fieldName, Object mock) {
		try {
			var field = TelegramBot.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(telegramBot, mock);
		} catch (Exception e) {
			fail("Failed to inject mock for field: " + fieldName);
		}
	}

	private Object callPrivateMethod(String methodName, Class<?>[] paramTypes, Object[] params) {
		try {
			var method = TelegramBot.class.getDeclaredMethod(methodName, paramTypes);
			method.setAccessible(true);
			return method.invoke(telegramBot, params);
		} catch (Exception e) {
			fail("Failed to call private method: " + methodName + " due to: " + e.getMessage());
			return null;
		}
	}

	@Test
	public void testRegisterUser() throws Exception {
		Message message = mock(Message.class);
		Chat chat = mock(Chat.class);

		when(message.getChatId()).thenReturn(12345L);
		when(message.getChat()).thenReturn(chat);
		when(chat.getFirstName()).thenReturn("John");
		when(chat.getLastName()).thenReturn("Doe");
		when(chat.getUserName()).thenReturn("johndoe");

		when(userRepository.findById(12345L)).thenReturn(Optional.empty());

		Method registerUser = TelegramBot.class.getDeclaredMethod("registerUser", Message.class);
		registerUser.setAccessible(true);

		registerUser.invoke(telegramBot, message);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		User savedUser = userCaptor.getValue();

		assertEquals(12345L, savedUser.getChatId());
		assertEquals("John", savedUser.getFirstName());
		assertEquals("Doe", savedUser.getLastName());
		assertEquals("johndoe", savedUser.getUserName());
	}

	@Test
	public void testOnUpdateReceivedWithStartCommand() {
		Update update = new Update();
		Message message = mock(Message.class);

		when(message.getText()).thenReturn("/start");
		when(message.getChatId()).thenReturn(12345L);
		update.setMessage(message);

		telegramBot.onUpdateReceived(update);
	}

	@Test
	public void testSendCategoryOptions() throws Exception {
		long chatId = 12345L;

		List<Category> categories = List.of(
				new Category(1L, "Category 1"),
				new Category(2L, "Category 2")
		);
		when(categoryService.getAllCategories()).thenReturn(categories);

		Method sendCategoryOptions = TelegramBot.class.getDeclaredMethod("sendCategoryOptions", long.class);
		sendCategoryOptions.setAccessible(true);

		sendCategoryOptions.invoke(telegramBot, chatId);

		verify(categoryService, times(1)).getAllCategories();
	}


	@Test
	public void testSendQuestionFromApi() throws Exception {
		long chatId = 12345L;

		java.lang.reflect.Method method = TelegramBot.class.getDeclaredMethod("sendQuestionFromApi", long.class);
		method.setAccessible(true);

		method.invoke(telegramBot, chatId);

		verify(quizApiService, times(1)).fetchQuizQuestions(15);
	}

}
