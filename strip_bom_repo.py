import os

def strip_bom_from_file(file_path):
    try:
        with open(file_path, 'rb') as f:
            content = f.read()
        if content.startswith(b'\xef\xbb\xbf'):
            print(f"Stripping BOM from: {file_path}")
            with open(file_path, 'wb') as f:
                f.write(content[3:])
            return True
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
    return False

def main():
    root_dir = r"c:\Users\USUARIO\Downloads\bicis_mx\bici\punto-mx"
    count = 0
    for root, dirs, files in os.walk(root_dir):
        # Skip directories like .git or target
        if '.git' in root or 'target' in root:
            continue
        for file in files:
            ext = os.path.splitext(file)[1].lower()
            if ext in ['.bs', '.xml', '.properties', '.txt', '.sql', '.java', '.html']:
                full_path = os.path.join(root, file)
                if strip_bom_from_file(full_path):
                    count += 1
    print(f"Done. Stripped BOM from {count} files.")

if __name__ == '__main__':
    main()
