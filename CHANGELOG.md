<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IsortConnect Changelog
## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [0.0.9] - 2020-02-05
### Fixed
- check refreshes

## [0.0.8] - 2020-02-05
### Changed

- changed check for changes
- check if file is writable in listener

## [0.0.7] - 2020-02-04 

### Added
- handle external changes

## [0.0.6] - 2020-02-03

### Added
- sort imports on move events

## [0.0.5] - 2020-01-25

### Fixed

- only first changed file was picked up
- start sending pyproject.toml file location on disk, so it can be resolved by isortd service

### Security

## [0.0.4]

### Added

- migrate to pycharm 2020.3

### Fixed

- ping dialog modal now invoked with modality state targeting settings pane

## [0.0.3]

### Added

- config token

## [0.0.2] - 2020-11-25

### Fixed

- check connection dialog crash

## [0.0.1] - 2020-11-25

### Added

- initial release
- config
- listen to pyproject.toml changes
- listen to *.py file changes
- format imports action
