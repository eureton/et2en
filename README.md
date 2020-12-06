# et2en

Translate from Estonian to English on the command line.

## Installation

Clone this repo:

```
    $ git clone https://github.com/eureton/et2en.git
    $ cd et2en/
````

## Usage

1. Setup `JAVA_HOME`.
1. Add the `$JAVA_HOME/bin` directory to your `PATH`.
1. Run the standalone jar with words as arguments:

  ```
      $ java -jar et2en-0.1.0-standalone.jar [options] [word1 [word2 ...]]
  ```

## Options

`-v` / `--version` displays the program version

`-h` / `--help` displays the help message

## Examples

In Estonian, the word `porise` could be either:

* the adjective `porine` in genitive singular or
* the verb `porisema` in 2nd person singular imperative

```
    $ java -jar et2en-0.1.0-standalone.jar porise

|   word | pos |    lemma |         gram |              definition |
|--------+-----+----------+--------------+-------------------------|
| porise | adj |   porine |         sg g | muddy, soiled, begrimed |
|        |   v | porisema | 2nd-p sg imp |           gnarl, witter |

```

### Bugs

## License

Copyright © 2020 Eureton OÜ

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
