package me.vickychijwani.spectre.model;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import me.vickychijwani.spectre.model.entity.ETag;
import me.vickychijwani.spectre.model.entity.Post;

public class DatabaseMigration implements RealmMigration {

    private static final String TAG = DatabaseMigration.class.getSimpleName();

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            if (schema.get("Post").isNullable("slug")) {
                // get rid of null-valued slugs, if any exist
                RealmResults<DynamicRealmObject> postsWithNullSlug = realm
                        .where(Post.class.getSimpleName())
                        .isNull("slug")
                        .findAll();
                for (DynamicRealmObject obj : postsWithNullSlug) {
                    obj.setString("slug", "");
                }
                // finally, make the field required
                schema.get("Post").setNullable("slug", false);
            }

            schema.get("Post")
                    .setNullable("html", true)
                    .setNullable("image", true)
                    .setNullable("createdAt", true)
                    .setNullable("publishedAt", true)
                    .setNullable("metaTitle", true)
                    .setNullable("metaDescription", true);
            schema.get("User")
                    .addIndex("id")
                    .setNullable("image", true)
                    .setNullable("bio", true);
            schema.get("Tag")
                    .setNullable("slug", true)
                    .setNullable("description", true)
                    .setNullable("image", true)
                    .setNullable("metaTitle", true)
                    .setNullable("metaDescription", true)
                    .setNullable("createdAt", true)
                    .setNullable("updatedAt", true);
            schema.get("Setting")
                    .addIndex("id");
            ++oldVersion;
        }

        if (oldVersion == 1) {
            // delete all etags, so the info can be fetched and stored
            // again, with role-based permissions enforced
            RealmResults<DynamicRealmObject> allEtags = realm
                    .where(ETag.class.getSimpleName())
                    .equalTo("type", ETag.TYPE_CURRENT_USER)
                    .or()
                    .equalTo("type", ETag.TYPE_ALL_POSTS)
                    .findAll();
            allEtags.deleteAllFromRealm();

            if (!schema.contains("Role")) {
                // create the Role table
                schema.create("Role")
                        .addField("id", Integer.class, FieldAttribute.PRIMARY_KEY)
                        .addField("uuid", String.class, FieldAttribute.REQUIRED)
                        .addField("name", String.class, FieldAttribute.REQUIRED)
                        .addField("description", String.class, FieldAttribute.REQUIRED);
            }

            if (!schema.get("User").hasField("roles")) {
                schema.get("User").addRealmListField("roles", schema.get("Role"));
            }
            ++oldVersion;
        }

        if (oldVersion == 2) {
            if (!schema.get("Post").hasField("conflictState")) {
                schema.get("Post").addField("conflictState", String.class, FieldAttribute.REQUIRED);
            }
            ++oldVersion;
        }
    }

}
