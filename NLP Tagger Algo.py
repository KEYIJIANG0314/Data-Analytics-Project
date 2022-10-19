
from abc import abstractmethod, ABC
from collections import Counter, defaultdict
from math import log
from operator import itemgetter
from typing import Any, Generator, Iterable, Sequence

############################################################
NEG_INF = float("-inf")


# DO NOT MODIFY
class Token:
    """Stores the text and tag for a token.

    Hashable and cleaner than indexing tuples all the time.
    """

    def __init__(self, token: str, tag: str):
        self.text = token
        self.tag = tag

    def __str__(self):
        return f"{self.text}/{self.tag}"

    def __repr__(self):
        return f"<Token {str(self)}>"

    def __eq__(self, other: Any):
        return (
                isinstance(other, Token) and self.text == other.text and self.tag == other.tag
        )

    def __lt__(self, other: "Token"):
        return self.to_tuple() < other.to_tuple()

    def __hash__(self):
        return hash(self.to_tuple())

    def to_tuple(self):
        """Return the text and tag as a tuple.

        Example:
        >>> token = Token("apple", "NN")
        >>> token.to_tuple()
        ('apple', 'NN')
        """
        return self.text, self.tag

    @staticmethod
    def from_tuple(t: tuple[str, ...]):
        """
        Creates a Token object from a tuple.
        """
        assert len(t) == 2
        return Token(t[0], t[1])

    @staticmethod
    def from_string(s: str) -> "Token":
        """Create a Token object from a string with the format 'token/tag'.

        Sample usage: Token.from_string("cat/NN")
        """
        return Token(*s.rsplit("/", 1))


class Tagger(ABC):
    @abstractmethod
    def train(self, sentences: Iterable[Sequence[Token]]) -> None:
        """Train the part of speech tagger by collecting needed counts from sentences."""
        raise NotImplementedError

    @abstractmethod
    def tag_sentence(self, sentence: Sequence[str]) -> list[str]:
        """
        Tags a sentence with part of speech tags.
        Sample usage:
            tag_sentence(["I", "ate", "an", "apple"])
             returns: ["PRP", "VBD", "DT", "NN"]
        """
        raise NotImplementedError

    def tag_sentences(
            self, sentences: Iterable[Sequence[str]]
    ) -> Generator[list[str], None, None]:
        """
        Tags each sentence's tokens with part of speech tags and
        yields the corresponding list of part of speech tags.
        """
        for sentence in sentences:
            yield self.tag_sentence(sentence)

    def test(
            self, tagged_sents: Iterable[Sequence[Token]]
    ) -> tuple[list[str], list[str]]:
        """
        Runs the tagger over all the sentences and returns a tuple with two lists:
        the predicted tag sequence and the actual tag sequence.
        The predicted and actual tags can then be used for calculating accuracy or other
        metrics.
        This does not preserve sentence boundaries.
        """
        predicted: list[str] = []
        actual: list[str] = []
        for sent in tagged_sents:
            predicted.extend(self.tag_sentence([t.text for t in sent]))
            actual.extend([t.tag for t in sent])
        return predicted, actual


def _safe_log(n: float) -> float:
    """Return the log of a number or -inf if the number is zero."""
    return NEG_INF if n == 0.0 else log(n)


def _max_item(scores: dict[str, float]) -> tuple[str, float]:
    """Return the key and value with the highest value."""
    # PyCharm gives a false positive type error here
    # noinspection PyTypeChecker
    return max(scores.items(), key=itemgetter(1))


def _most_frequent_item(counts: Counter[str]) -> str:
    """Return the most frequent item in a Counter."""
    assert counts, "Counter is empty"
    top_item, _ = counts.most_common(1)[0]
    return top_item


############################################################


class MostFrequentTagTagger(Tagger):
    def __init__(self):
        self.default_tag = str

    def train(self, sentences: Iterable[Sequence[Token]]) -> None:
        tags_counts = Counter()
        for sentence in sentences:
            for token in sentence:
                tags_counts[token.tag] += 1
        self.default_tag = _most_frequent_item(tags_counts)

    def tag_sentence(self, sentence: Sequence[str]) -> list[str]:
        size = len(sentence)
        result = [self.default_tag] * size
        return result


class UnigramTagger(Tagger):
    def __init__(self):
        self.default_text_tag = defaultdict(str)
        self.default_tag = str

    def train(self, sentences: Iterable[Sequence[Token]]):
        text_tags_counts = defaultdict(Counter)
        tags_counts = Counter()
        for sentence in sentences:
            for token in sentence:
                tags_counts[token.tag] += 1
                text_tags_counts[token.text][token.tag] += 1
        self.default_tag = _most_frequent_item(tags_counts)
        for key in text_tags_counts:
            self.default_text_tag[key] = _most_frequent_item(text_tags_counts[key])

    def tag_sentence(self, sentence: Sequence[str]) -> list[str]:
        result = []
        for word in sentence:
            if word not in self.default_text_tag:
                result.append(self.default_tag)
            else:
                result.append(self.default_text_tag[word])
        return result


class SentenceCounter:
    def __init__(self, k: float):
        self.k = k
        self.unique_tags = list()
        self.tag_text_emission = defaultdict(Counter)
        self.tag_text_number = defaultdict(int)
        self.tag_text_unique = defaultdict(int)
        self.tag_tag_transition = defaultdict(Counter)
        self.tag_number = defaultdict(int)
        self.initial_num = 0
        self.initial_tag = defaultdict(int)

    def count_sentences(self, sentences: Iterable[Sequence[Token]]) -> None:
        """Count token text and tags in sentences.

        After this function runs the SentenceCounter object should be ready
        to return values for initial, transition, and emission probabilities
        as well as return the sorted tagset.
        """
        unique_tags = set()
        for sentence in sentences:
            sentence = list(sentence)
            if sentence:
                self.initial_tag[sentence[0].tag] += 1
            unique_text = set()
            for i in range(len(sentence) - 1):
                unique_tags.add(sentence[i].tag)
                self.tag_number[sentence[i].tag] += 1
                self.tag_text_number[sentence[i].tag] += 1
                if sentence[i].text not in unique_text:
                    unique_text.add(sentence[i].text)
                    self.tag_text_unique[sentence[i].tag] += 1
                self.tag_text_emission[sentence[i].tag][sentence[i].text] += 1
                self.tag_tag_transition[sentence[i].tag][sentence[i + 1].tag] += 1
            self.tag_text_emission[sentence[-1].tag][sentence[-1].text] += 1
            self.tag_text_number[sentence[-1].tag] += 1
            if sentence[-1].text not in unique_text:
                self.tag_text_unique[sentence[-1].tag] += 1
        self.unique_tags = list(unique_tags)
        self.unique_tags.sort()
        self.initial_num = sum(self.initial_tag.values())

    def tagset(self) -> list[str]:
        return self.unique_tags

    def emission_prob(self, tag: str, word: str) -> float:
        return (self.tag_text_emission[tag][word] + self.k) / (
                self.tag_text_number[tag] + (self.k * self.tag_text_unique[tag]))

    def transition_prob(self, prev_tag: str, current_tag: str) -> float:
        return self.tag_tag_transition[prev_tag][current_tag] / self.tag_number[prev_tag]

    def initial_prob(self, tag: str) -> float:
        if self.initial_num != 0:
            return self.initial_tag[tag] / self.initial_num


class BigramTagger(Tagger, ABC):
    def __init__(self, k: float) -> None:
        self.counter = SentenceCounter(k)

    def train(self, sents: Iterable[Sequence[Token]]) -> None:
        self.counter.count_sentences(sents)

    def sequence_probability(self, sentence: Sequence[str], tags: Sequence[str]) -> float:
        sentence = list(sentence)
        tags = list(tags)
        result = _safe_log(self.counter.initial_prob(tags[0]))
        for i in range(len(sentence) - 1):
            result += _safe_log(self.counter.emission_prob(tags[i], sentence[i]))
            result += _safe_log(self.counter.transition_prob(tags[i], tags[i + 1]))
        result += _safe_log(self.counter.emission_prob(tags[-1], sentence[-1]))
        return result


class GreedyBigramTagger(BigramTagger):

    def tag_sentence(self, sentence: Sequence[str]) -> list[str]:
        sentence = list(sentence)
        result = []
        initial_tag = _max_item({tag: self.counter.initial_prob(tag) * self.counter.emission_prob(tag, sentence[0])
                                 for tag in self.counter.unique_tags})
        prev_tag = initial_tag[0]
        result.append(prev_tag)
        for i in range(1, len(sentence)):
            next_tag = _max_item({tag: self.counter.transition_prob(prev_tag, tag) * self.counter.emission_prob(
                                        tag, sentence[i]) for tag in self.counter.unique_tags})
            prev_tag = next_tag[0]
            result.append(prev_tag)
        return result


class ViterbiBigramTagger(BigramTagger):

    def tag_sentence(self, sentence: Sequence[str]) -> list[str]:
        sentence = list(sentence)
        result = []
        back_pointer = []
        best_scores = [{tag: self.counter.initial_prob(tag) * self.counter.emission_prob(tag, sentence[0]) for tag in
                        self.counter.unique_tags}]
        for i in range(1, len(sentence)):
            temp_score_dict = defaultdict(float)
            temp_pointer_dict = defaultdict(str)
            for tag in self.counter.unique_tags:
                transition_score_dict = {prev_tag: self.counter.emission_prob(tag, sentence[i]) *
                                                   best_scores[i-1][prev_tag] *
                                                   self.counter.transition_prob(prev_tag, tag)
                                                   for prev_tag in self.counter.unique_tags}
                temp_score_dict[tag] = _max_item(transition_score_dict)[1]
                temp_pointer_dict[tag] = _max_item(transition_score_dict)[0]
            best_scores.append(temp_score_dict)
            back_pointer.append(temp_pointer_dict)
        final_tag = _max_item(best_scores[-1])[0]
        result.append(final_tag)
        for j in range(len(back_pointer)-1, -1, -1):
            back_tag = back_pointer[j][final_tag]
            result.append(back_tag)
            final_tag = back_tag
        result.reverse()
        return result
