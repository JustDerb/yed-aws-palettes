# AWS Simple Icons Palettes for yED

[yED](http://www.yworks.com/en/products/yfiles/yed/) is an awesome diagraming software. [AWS Simple Icons](https://aws.amazon.com/architecture/icons/) is an AWS-created icon set for use in architecture diagrams.

This repository contains pre-made palettes to import into yED to start diagramming with AWS Icons immediately!

![Screenshot](screenshot.png)

# Support

## Updating yED with new icons

You can use "Import Section..." in yED as described [here](http://yed.yworks.com/support/manual/palette_manager.html).

- Inside yED, go to "Edit" > "Manage Palette..."
- Select "Import Section..."
- In the file dialog, select all of the `.graphml` files and select "Okay"

This should override any sections with the same name in yED.

### Deleting old palettes

You can use "Delete Section" in yED as described [here](http://yed.yworks.com/support/manual/palette_manager.html).

- Inside yED, go to "Edit" > "Manage Palette..."
- Select a single palette you want to delete
- Select "Delete Section"

## Updating this repository

This repo has minimal automation around it, so it should update at most daily. If this repo needs to be manually updated, simply grab the latest URL and run the `update.sh` script:

```bash
# Run the updater, automatically finding the latest URL, commiting the results
./update.sh auto true
```
