package tpcreative.co.qrscanner.model;

import java.io.Serializable;

public class StorageQuota implements Serializable {
      public long limit;
      public long usage;
      public long usageInDrive;
      public long usageInDriveTrash;
}
