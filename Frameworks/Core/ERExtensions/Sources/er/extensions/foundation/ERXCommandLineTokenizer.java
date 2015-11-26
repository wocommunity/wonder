package er.extensions.foundation;

import java.text.CharacterIterator;
import java.text.ParseException;
import java.text.StringCharacterIterator;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Tokenizes a string like a commandline parser, tokenizing on spaces unless the words are 
 * in double quotes or single quotes. 
 * 
 * @author mschrag
 */
public class ERXCommandLineTokenizer implements Enumeration {
  private static enum TokenizerState {
    Whitespace, Text, DoubleQuoted, SingleQuoted
  }

  private StringCharacterIterator _iterator;
  private TokenizerState _state;
  private boolean _wasQuoted;

  /**
   * Creates a new ERXCommandLineTokenizer.
   * 
   * @param line the line to parse
   */
  public ERXCommandLineTokenizer(String line) {
    _iterator = new StringCharacterIterator(line);
    reset();
  }

  protected void reset() {
    _state = TokenizerState.Whitespace;
    _iterator.first();
  }

  /**
   * Returns true if there are more tokens on the line.
   * 
   * @return true if there are more tokens on the line, false if not
   */
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  /**
   * Returns true if there are more tokens on the line.
   * 
   * @return true if there are more tokens on the line, false if not
   */
  public boolean hasMoreTokens() {
    return (_iterator.current() != CharacterIterator.DONE);
  }

  /**
   * Returns the next token, or null if there is a parse error.
   * 
   * @return the next token
   */
  public String nextElement() {
    String token;
    try {
      token = nextToken();
    }
    catch (ParseException e) {
      e.printStackTrace();
      token = null;
    }
    return token;
  }

  /**
   * Returns the next token.
   * 
   * @return the next token
   * @throws ParseException if there is a parse failure
   * @throws NoSuchElementException if there are no more tokens to parse
   */
  public String nextToken() throws ParseException {
    boolean escapeNext = false;
    boolean wasQuoted = _wasQuoted;

    StringBuilder token = new StringBuilder();
    char c = _iterator.current();
    boolean done = false;
    while (!done && c != CharacterIterator.DONE) {
      if (escapeNext) {
        switch (c) {
        case '\n':
          throw new ParseException("Unexception escape '\\' at end of string.", _iterator.getIndex());

        default:
          token.append(c);
          c = _iterator.next();
          break;
        }
        escapeNext = false;
      }
      else {
        switch (_state) {
        case Whitespace:
          switch (c) {
          case '\n':
          case ' ':
          case '\t':
            c = _iterator.next();
            break;

          case '\"':
            _state = TokenizerState.DoubleQuoted;
            c = _iterator.next();
            if (token.length() > 0 || _wasQuoted) {
              done = true;
              _wasQuoted = false;
            }
            _wasQuoted = true;
            break;

          case '\'':
            _state = TokenizerState.SingleQuoted;
            c = _iterator.next();
            if (token.length() > 0 || _wasQuoted) {
              done = true;
              _wasQuoted = false;
            }
            _wasQuoted = true;
            break;

          case '\\':
            escapeNext = true;
            c = _iterator.next();
            break;

          default:
            _state = TokenizerState.Text;
            if (token.length() > 0 || _wasQuoted) {
              done = true;
              _wasQuoted = false;
            }
            break;
          }
          break;

        case Text:
          switch (c) {
          case ' ':
          case '\t':
          case '\n':
            _state = TokenizerState.Whitespace;
            break;

          // case '\"':
          // throw new ParseException("Unexpected quote '\"' in string.",
          // myIterator.getIndex());

          case '\\':
            escapeNext = true;
            c = _iterator.next();
            break;

          default:
            token.append(c);
            c = _iterator.next();
            break;
          }
          break;

        case DoubleQuoted:
          switch (c) {
          case '\"':
            _state = TokenizerState.Whitespace;
            c = _iterator.next();
            break;

          case '\\':
            escapeNext = true;
            c = _iterator.next();
            break;

          default:
            token.append(c);
            c = _iterator.next();
            break;
          }
          break;

        case SingleQuoted:
          switch (c) {
          case '\'':
            _state = TokenizerState.Whitespace;
            c = _iterator.next();
            break;

          case '\\':
            escapeNext = true;
            c = _iterator.next();
            break;

          default:
            token.append(c);
            c = _iterator.next();
            break;
          }
          break;
        }
      }
    }

    if (token.length() <= 0 && !wasQuoted) {
      throw new NoSuchElementException("There are no more tokens on this line.");
    }

    return token.toString();
  }
}