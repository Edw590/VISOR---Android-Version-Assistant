# Commit conventions list
## Main note
Before each convention name is a number. That number is the version of the convention to be put on the subject, body and
footer.

## Version 1
The 3rd of the commit must start with "1;" - the version of the convention in use on the commit.
A line with only "===" must be before the body and the footer(s) - always put them even if the corresponding component
will not be used in the commit.

### Subject:
Use the native Conventional Commits v1.0.0.

### Body:
Use the type words from Conventional Commits v1.0.0 plus the idea in Javadoc that makes everything before the first
dot(.) to be the subject. In this case, it's the thing that will be discussed in that point in the body (if there's
anything to discuss).

### Footer(s):
Use the native Conventional Commits v1.0.0.

### Example of use
```
Add: something that doesn't come to my mind now

1
===
List of major changes:
- Add: links to useful documents and webpages in a new folder.
  Now all useful and hard-to-find stuff is in a special folder.
- Add: commit convention (check the conventions list on the file).

===
Reviewed-by: Z
Closes: #123, #456
See-also: #789
```
