import re
import sys
import os

def main():
    version_file = "VERSION.txt"
    if not os.path.exists(version_file):
        print(f"Error: {version_file} not found.")
        sys.exit(1)
        
    with open(version_file, "r", encoding="utf-8") as f:
        version_str = f.read().strip()
        
    print(f"Current version in VERSION.txt: '{version_str}'")
    
    # Matches: major.minor.patch followed optionally by a suffix
    # e.g., 1.0.0-SNAPSHOT, 1.0.1-Sebastian, 1.0.0, etc.
    match = re.match(r'^(\d+)\.(\d+)\.(\d+)(?:-(.*))?$', version_str)
    if not match:
        print("Warning: Version format is not standard X.Y.Z-suffix. Defaulting to 1.0.1-Sebastian.")
        new_version = "1.0.1-Sebastian"
    else:
        major, minor, patch, suffix = match.groups()
        new_patch = int(patch) + 1
        new_version = f"{major}.{minor}.{new_patch}-Sebastian"
        
    print(f"Bumping version to: '{new_version}'")
    
    with open(version_file, "w", encoding="utf-8") as f:
        f.write(new_version + "\n")
        
    print("VERSION.txt updated successfully.")
    
    # We can output the new version for GitHub Actions steps
    if "GITHUB_OUTPUT" in os.environ:
        with open(os.environ["GITHUB_OUTPUT"], "a") as out:
            out.write(f"new_version={new_version}\n")

if __name__ == "__main__":
    main()
