# API and UI Binding Creation Guideline

## Purpose
This document describes the steps to add an API using the MVVM architecture and bind the data to the UI according to the Material3 guidelines.

When using generative AI, add the guideline below and then write the prompt.

## Requirements

1. **Create DTO**
   - Create a DTO that matches the API document

2. **API interface**
   - Add the API call function inside an already-declared API interface
   - Create a new one only if no appropriate API interface exists

3. **API call function**
   - Implement an API call function that matches the API document

4. **ViewModel integration**
   - Add the API call function to the appropriate ViewModel
   - Call the API through the ViewModel and store the data
   - Render the UI with the stored data

## Constraints

1. **Imports**
   - Check the existing versions, then use imports

2. **Responsiveness**
   - Ensure the code is responsive

## Expected Result

1. **API interface**
   - The API function is added to the API interface

2. **ViewModel**
   - The API call function exists in the appropriate ViewModel
   - Data is stored after the API call

3. **UI rendering**
   - Draw the UI with the stored data

## Additional Notes

- Follow the MVVM architecture and Material3 guidelines
- Implement consistently with the project's existing patterns and rules
