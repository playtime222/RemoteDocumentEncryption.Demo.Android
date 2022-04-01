package nl.rijksoverheid.rdw.rde.clientlib.messaging;

import java.util.ArrayList;

/**
 * Pass this and a crypto spec to a message formatter
 * TODO use a builder?
 */
public class MessageContentArgs
{
    private String unencryptedNote;
    private final ArrayList<FileArgs> files = new ArrayList<>();

    public String getUnencryptedNote() { return unencryptedNote; }
    public void setUnencryptedNote(final String unencryptedNote)
    {
        this.unencryptedNote = unencryptedNote;
    }

    //public FileArgs getFileArgs(int index) { return files.get(index); }
    public FileArgs[] getFileArgs() {

        var result = new FileArgs[files.size()];
        for(var i = 0; i<files.size();i++)
            result[i] = files.get(i);

        return result;
    }

    public void add(final FileArgs file)
    {
        files.add(file);
    }


    }

