Still to do before localization is complete:

#### Site
- Need to figure out why square bracket links are wrong only when translated.
- Need to translate a few more pages manually to ensure everything is applying correctly.
- Reverse segment application is applying in some cases when it shouldn't (for instance CommandHelper_Logo_New.png)

#### l10n-ui
- Pull Requests aren't implemented.
- Find Segment menu isn't implemented.
- Ensure Help file is complete and correct.
- Create reload from file system option
- Need to create orphan segment deletion tool
- Add Push Only to tool

#### Segments
- Learning trail isn't translated at all yet.
- Some <%templates%> that could be translated aren't added to the segments list yet.

#### Pipelines
- Create PR validation pipeline, which ensures, among other things, that the xml is valid, no new segments were added 
or deleted, and the text is generally coherent.