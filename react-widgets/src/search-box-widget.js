import React from 'react';
import ReactDOM from 'react-dom';
import EventSearch from "./EventSearch";

const widget = document.getElementById("tm-event");
const draft = widget.dataset.proposalDraft
const error = widget.dataset.error
const draftId = widget.dataset.draftId

ReactDOM.render(<EventSearch draft={draft} error={error} draftId={draftId} />, widget);