package server;

import java.util.Random;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import server.api.BoardWebsocketHandler;
import server.api.MessageBroker;
import server.database.BoardRepository;
import server.database.CardListRepository;
import server.database.CardRepository;
import server.database.ColorPresetRepository;
import server.database.SubtaskRepository;
import server.database.TagRepository;

@Configuration
@EnableWebSocket
@EnableJpaRepositories("server.database")
public class Config implements WebSocketConfigurer {
    private final BoardRepository boardRepository;
    private final CardListRepository cardListRepository;
    private final CardRepository cardRepository;
    private final TagRepository tagRepository;
    private final ColorPresetRepository colorPresetRepository;
    private final SubtaskRepository subtaskRepository;
    private final MessageBroker messageBroker;

    /**
     * Constructor.
     *
     * @param boardRepository       The board repository.
     * @param cardListRepository    The card list repository.
     * @param cardRepository        The card repository.
     * @param tagRepository         The tag repository.
     * @param colorPresetRepository The color preset repository.
     * @param subtaskRepository     The subtask repository.
     */
    public Config(
            final BoardRepository boardRepository,
            final CardListRepository cardListRepository,
            final CardRepository cardRepository,
            final TagRepository tagRepository,
            final ColorPresetRepository colorPresetRepository,
            final SubtaskRepository subtaskRepository
    ) {
        this.boardRepository = boardRepository;
        this.cardListRepository = cardListRepository;
        this.cardRepository = cardRepository;
        this.tagRepository = tagRepository;
        this.colorPresetRepository = colorPresetRepository;
        this.subtaskRepository = subtaskRepository;

        this.messageBroker = new MessageBroker();
    }

    /**
     * The random bean.
     *
     * @return a new instance of {@code random}.
     */
    @Bean
    public Random getRandom() {
        return new Random();
    }

    /**
     * Registers the websocket handler for the registry.
     *
     * @param registry The registry.
     */
    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(new BoardWebsocketHandler(boardRepository, messageBroker), "/board");
    }

    /**
     * Bean for BoardRepository.
     *
     * @return The BoardRepository.
     */
    @Bean
    public BoardRepository getBoardRepository() {
        return boardRepository;
    }

    /**
     * Bean for CardListRepository.
     *
     * @return The CardListRepository.
     */
    @Bean
    public CardListRepository getCardListRepository() {
        return cardListRepository;
    }

    /**
     * Bean for CardRepository.
     *
     * @return The CardRepository.
     */
    @Bean
    public CardRepository getCardRepository() {
        return cardRepository;
    }

    /**
     * Bean for TagRepository.
     *
     * @return The TagRepository.
     */
    @Bean
    public TagRepository getTagRepository() {
        return tagRepository;
    }

    /**
     * Bean for ColorPresetRepository.
     *
     * @return The ColorPresetRepository.
     */
    @Bean
    public ColorPresetRepository getColorPresetRepository() {
        return colorPresetRepository;
    }

    /**
     * Bean for SubtaskRepository.
     *
     * @return The SubtaskRepository.
     */
    @Bean
    public SubtaskRepository getSubtaskRepository() {
        return this.subtaskRepository;
    }

    /**
     * Bean for MessageBroker.
     *
     * @return The MessageBroker.
     */
    @Bean
    public MessageBroker messageBroker() {
        return messageBroker;
    }
}
