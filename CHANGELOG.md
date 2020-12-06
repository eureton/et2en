# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Changed

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

[Unreleased]: https://github.com/eureton/et2en/compare/0.1.0...HEAD
