import os

MY_FILES = "src/main/java/com/github/zinoviy23"
MY_FILES_TAPAAL = "src/com/github/zinoviy23"

p = ('    \\subsection{%s}\n'
     '    \\lstinputlisting[language=Java]{%s}'
)

for subdir, dirs, files in os.walk('../../'):
    for file in files:
        f = os.path.join(subdir, file)

        if f.endswith('.java') and (MY_FILES in f or MY_FILES_TAPAAL in f):
            print(p % (file, f))