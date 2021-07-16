# Commit conventions list

## Table of Contents
- [Reason of this list](#reason-of-this-list)
- [Version 0](#version-0)
- [Version 1](#version-1)

## Main notes
The reason for this file is if it's ever needed to make an automated program to search for commits and/or generate changelogs, for example, here are the conventions used in each commit made.
For subversions, only the changed points shall be documented.

## Version 0
What was used until version 1 - didn't pay any attention to it and just wrote as I saw that could be a good way (except when I'd commit directly from GitHub, where I'd use their automatic conventions).

### Subject:
No convention used. Random. No main words on manually written commits. There were also GitHub automatically-written commits, which use their conventions.

### Body:
2 options:
- No body.
- A line with a sub-title, and then points explaining each thing. Might have too a line or 2 before the first one mentioned saying anything.

### Footer(s):
No footer.

### Example:
```
PLS working now + various changes

Whatever I wanted to say here.
List of changes:
- Fixed this
- Because of stuff, updated that. That's because I didn't know what I was doing.
  Actually, still don't.
  This is just to note I'm writing various lines inside the same point.
```

## Version 1
The 3rd of the commit must start with "[VERSION];" - [VERSION] is the convention version in use on the commit ("1" in this case).
A line with only "\=\=\=" must be before the body and the footer(s) - always put them even if the corresponding component will not be used in the commit. The footer "\=\=\=" should have a blank line before it (optional - it's just for the looks).

Note: I just started with Git. Probably this first version is assuming a commit is a big thing and maybe it's not. I'll change this with time, if it's needed.

### Subject:
Use the native Conventional Commits v1.0.0.

### Body:
The body can have sub-titles ended with a colon, and below, points explaining each thing. For each point, use the type words from Conventional Commits v1.0.0 plus the idea in Javadoc that makes everything before the first dot(.) to be the subject. In this case, it's the thing that will be discussed in that point in the body (if there's anything to discuss). After that first sentence, a paragraph should come (optional).
The body can also have perfectly normal texts and sentences. Doesn't need to have only points to discuss (if it has any).

### Footer(s):
Use the native Conventional Commits v1.0.0.

### Example:
```
Add: something that doesn't come to my mind now

[VERSION];
===
List of major changes:
- Add: links to useful documents and webpages in a new folder.
  Now all useful and hard-to-find stuff is in a special folder.
- Add: commit convention (check the conventions list on the file).

Whatever sub-title:
- Update: this and that.
  Because it was needed since.... Who knows? xD
- Add: that.

After this commit, people can finally fly!

===
Bug-ID: w4ht3v3r
Reviewed-by: Z
Closes: #123, #456
See-also: #789
```

## Version 1.1

### Subject:
The colons may or may not exist. If they do exist, they come after the first word - which, if more than one word is needed, must have dashes instead of spaces. The reason for this is the automatic convention used on GitHub (so no need to rewrite the subject if the default one is good enough - like for editting a file).
