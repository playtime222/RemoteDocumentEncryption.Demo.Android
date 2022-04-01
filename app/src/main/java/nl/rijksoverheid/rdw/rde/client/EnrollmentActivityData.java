//package nl.rijksoverheid.rdw.rde.client;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
//public class EnrollmentActivityData implements Parcelable
//{
//    private String enrollmentId;
//    private String displayName;
//
//    //BACKey info
//    private String documentId;
//    private String dateOfBirth;
//    private String dateExpiry;
//
//    public String getDisplayName()
//    {
//        return displayName;
//    }
//
//    public String getDocumentId()
//    {
//        return documentId;
//    }
//
//    public void setDocumentId(final String documentId)
//    {
//        this.documentId = documentId;
//    }
//
//    public void setDisplayName(final String displayName)
//    {
//        this.displayName = displayName;
//    }
//
//    public String getDateOfBirth()
//    {
//        return dateOfBirth;
//    }
//
//    public void setDateOfBirth(final String dateOfBirth)
//    {
//        this.dateOfBirth = dateOfBirth;
//    }
//
//    public String getDateExpiry()
//    {
//        return dateExpiry;
//    }
//
//    public void setDateExpiry(final String dateExpiry)
//    {
//        this.dateExpiry = dateExpiry;
//    }
//
//    public String getEnrollmentId()
//    {
//        return enrollmentId;
//    }
//
//    public void setEnrollmentId(final String enrollmentId)
//    {
//        this.enrollmentId = enrollmentId;
//    }
//
//    public EnrollmentActivityData()
//    {}
//
//    // Parcelling part
//    public EnrollmentActivityData(final Parcel in)
//    {
//        if (in == null)
//            throw new IllegalArgumentException();
//
//        var data = in.createStringArray();
//
//        if (data.length != 5)
//            return;
//
//        this.enrollmentId = data[0];
//        this.displayName = data[1];
//        this.documentId = data[2];
//        this.dateOfBirth = data[3];
//        this.dateExpiry = data[4];
//    }
//
//    @Override
//    public int describeContents(){
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(final Parcel dest, final int flags) {
//
//        if (dest == null)
//            throw new IllegalArgumentException();
//
//        dest.writeStringArray(new String[] {
//        this.enrollmentId,
//        this.displayName,
//        this.documentId,
//        this.dateOfBirth,
//        this.dateExpiry,
//        });
//    }
//    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
//        public EnrollmentActivityData createFromParcel(final Parcel in) {
//            if (in == null)
//                throw new IllegalArgumentException();
//
//            return new EnrollmentActivityData(in);
//        }
//
//        public EnrollmentActivityData[] newArray(final int size) {
//            return new EnrollmentActivityData[size];
//        }
//    };
//}
