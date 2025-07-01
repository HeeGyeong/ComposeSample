#!/bin/bash

# Base package path
BASE_PACKAGE="com.example.composesample.presentation.example.component"

# Find all .kt files and update their package declarations
find . -name "*.kt" -type f | while read file; do
    # Get the directory path relative to current directory
    dir_path=$(dirname "$file")
    
    # Remove the leading "./"
    dir_path=${dir_path#./}
    
    # Convert directory path to package path
    if [ "$dir_path" = "." ]; then
        # Files in current directory
        new_package="$BASE_PACKAGE"
    else
        # Replace / with . for package notation
        package_suffix=$(echo "$dir_path" | sed 's/\//./g')
        new_package="$BASE_PACKAGE.$package_suffix"
    fi
    
    echo "Updating $file -> $new_package"
    
    # Update the package declaration (first line starting with "package")
    sed -i '' "1s/^package .*/package $new_package/" "$file"
done

echo "Package update completed!" 