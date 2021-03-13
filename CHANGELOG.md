# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Changed

## [1.0.7] - 2021-03-13

### Changed

- update Glosbe scraper to the latest HTML
- update `jsoup`
- update `clj-http`
- update `tools.cli`

## [1.0.6] - 2021-03-01

### Changed

- update Clojure to `1.10.2`

### Added

- `native-image` leiningen build target (works with GraalVM)

## [1.0.5] - 2021-01-12

### Added

- print abbreviations legend

### Fixed

- single-threaded execution bug
- sanitization results in blank input
- failure to scrape Glosbe

## [1.0.4] - 2020-12-08

### Changed
- strip irrelevant symbols from input, process remainder

## [1.0.2] - 2020-12-06

### Added
- argument deduplication
- identify part-of-speech for word lemmas
- transducer pipelines for per-word, per-lemma processing logic
- grammatical form identification
- parallel execution of processing pipelines
- intuitive output for unknown words
- input sanitization
- `-h` and `-v` cli options

### Changed
- migrated scraping code from `enlive` to `clj-http`

## [0.1.0] - 2020-11-25

### Added
- First draft implementation scrapes `glosbe.com` / `filosoft.ee` and pretty-prints the results.

### Changed

### Removed

### Fixed

[Unreleased]: https://github.com/eureton/et2en/compare/1.0.7...HEAD
[1.0.7]: https://github.com/eureton/et2en/compare/1.0.6...1.0.7
[1.0.6]: https://github.com/eureton/et2en/compare/1.0.5...1.0.6
[1.0.5]: https://github.com/eureton/et2en/compare/1.0.4...1.0.5
[1.0.4]: https://github.com/eureton/et2en/compare/1.0.2...1.0.4
[1.0.2]: https://github.com/eureton/et2en/compare/0.1.0...1.0.2
