import React from 'react';
import ReactDOM from 'react-dom';
import ProposalList from './ProposalList';
import "react-big-calendar/lib/css/react-big-calendar.css";

const widget = document.getElementById("proposal-list");
const proposals = widget.dataset.proposals
const drafts = widget.dataset.drafts
const uid = widget.dataset.uid
const error = widget.dataset.error

ReactDOM.render(<ProposalList proposals={proposals} drafts={drafts} error={error} uid={uid} />, widget);